/// <reference types="node" />

import assert from 'node:assert/strict';
import test from 'node:test';
import { getApiConfigurationError } from '@/src/lib/api-configuration';
import {
  getAllowedShipmentTransitions,
  isShipmentTransitionAllowed,
  prepareShipmentStatusUpdate,
  requiresShipmentStatusConfirmation,
} from '@/src/lib/shipment-workflow';
import {
  createSingleFlight,
  hasShipperRole,
  retryOnceAfterUnauthorized,
} from '@/src/lib/session-policy';
import type { ApiResult } from '@/src/types/api';

test('device API URL must end in /api and cannot point to a local-only host', () => {
  assert.equal(getApiConfigurationError('http://10.0.2.2:8080/api', 'android'), null);
  assert.match(getApiConfigurationError('http://10.0.2.2:8080', 'android') ?? '', /\/api/);
  assert.match(getApiConfigurationError('http://localhost:8080/api', 'android') ?? '', /localhost/);
  assert.match(getApiConfigurationError('http://127.0.0.1:8080/api', 'ios') ?? '', /localhost/);
});

test('shipment workflow follows the backend status transitions', () => {
  assert.deepEqual(getAllowedShipmentTransitions('ASSIGNED'), ['PICKED_UP', 'FAILED']);
  assert.deepEqual(getAllowedShipmentTransitions('PICKED_UP'), ['DELIVERING', 'FAILED']);
  assert.deepEqual(getAllowedShipmentTransitions('DELIVERING'), ['DELIVERED', 'FAILED']);
  assert.equal(isShipmentTransitionAllowed('ASSIGNED', 'DELIVERING'), false);
  assert.equal(isShipmentTransitionAllowed('DELIVERED', 'FAILED'), false);
});

test('failed status requires a trimmed reason and terminal actions require confirmation', () => {
  assert.throws(
    () => prepareShipmentStatusUpdate('DELIVERING', 'FAILED', '   '),
    /Nhap ly do that bai/,
  );
  assert.deepEqual(
    prepareShipmentStatusUpdate('DELIVERING', 'FAILED', '  Khach tu choi nhan  '),
    { status: 'FAILED', failureReason: 'Khach tu choi nhan' },
  );
  assert.equal(requiresShipmentStatusConfirmation('DELIVERED'), true);
  assert.equal(requiresShipmentStatusConfirmation('FAILED'), true);
  assert.equal(requiresShipmentStatusConfirmation('DELIVERING'), false);
});

test('role gate accepts only sessions that include SHIPPER', () => {
  assert.equal(hasShipperRole(['SHIPPER']), true);
  assert.equal(hasShipperRole(['ADMIN']), false);
  assert.equal(hasShipperRole(['STAFF', 'CUSTOMER']), false);
});

test('refresh uses single flight for concurrent callers', async () => {
  let calls = 0;
  let release: (() => void) | undefined;
  const waitForRelease = new Promise<void>((resolve) => {
    release = resolve;
  });
  const refresh = createSingleFlight(async () => {
    calls++;
    await waitForRelease;
    return 'new-access-token';
  });

  const first = refresh();
  const second = refresh();
  release?.();

  assert.equal(await first, 'new-access-token');
  assert.equal(await second, 'new-access-token');
  assert.equal(calls, 1);
});

test('a request retries at most once after refresh', async () => {
  let requestCalls = 0;
  let refreshCalls = 0;
  const send = async (): Promise<ApiResult<{ value: string }>> => {
    requestCalls++;
    return { ok: false, status: 401, body: null };
  };
  const initialResult = await send();
  const result = await retryOnceAfterUnauthorized(
    initialResult,
    { accessToken: 'old-access' },
    send,
    async () => {
      refreshCalls++;
      return { accessToken: 'new-access' };
    },
  );

  assert.equal(result.status, 401);
  assert.equal(requestCalls, 2);
  assert.equal(refreshCalls, 1);
});

import { palette } from '@/src/config';
import { formatCurrency, formatDateTime } from '@/src/lib/format';
import { getNextShipmentActions } from '@/src/lib/status';
import { prepareShipmentStatusUpdate, requiresShipmentStatusConfirmation } from '@/src/lib/shipment-workflow';
import { fetchShipmentById, updateShipmentStatus } from '@/src/services/shipments';
import type { Shipment, ShipmentStatus } from '@/src/types/shipment';
import { ActionButton, Panel, SectionTitle, StatusBadge } from '@/src/components/ui';
import { useSession } from '@/src/context/session-context';
import MaterialCommunityIcons from '@expo/vector-icons/MaterialCommunityIcons';
import { Redirect, Stack, useLocalSearchParams } from 'expo-router';
import { useEffect, useRef, useState } from 'react';
import {
  ActivityIndicator,
  Alert,
  KeyboardAvoidingView,
  Linking,
  Platform,
  ScrollView,
  StyleSheet,
  Text,
  TextInput,
  View,
} from 'react-native';

export default function ShipmentDetailScreen() {
  const { id } = useLocalSearchParams<{ id: string }>();
  const { hydrated, request, session } = useSession();
  const [shipment, setShipment] = useState<Shipment | null>(null);
  const [loading, setLoading] = useState(true);
  const [savingStatus, setSavingStatus] = useState<ShipmentStatus | null>(null);
  const [failureReason, setFailureReason] = useState('');
  const [error, setError] = useState<string | null>(null);
  const statusUpdateInFlightRef = useRef(false);

  useEffect(() => {
    if (!hydrated || !session || !id) {
      return;
    }

    void loadShipment();
  }, [hydrated, id, session]);

  if (hydrated && !session) {
    return <Redirect href="/login" />;
  }

  async function loadShipment() {
    if (!id) {
      return;
    }

    try {
      setLoading(true);
      setError(null);
      const nextShipment = await fetchShipmentById(request, id);
      setShipment(nextShipment);
      setFailureReason(nextShipment.failureReason ?? '');
    } catch (loadError) {
      setError(loadError instanceof Error ? loadError.message : 'Khong tai duoc chi tiet chuyen');
    } finally {
      setLoading(false);
    }
  }

  async function handleStatusUpdate(nextStatus: ShipmentStatus) {
    if (!shipment || statusUpdateInFlightRef.current) {
      return;
    }

    let payload;
    try {
      payload = prepareShipmentStatusUpdate(shipment.shipmentStatus, nextStatus, failureReason);
    } catch (validationError) {
      setError(validationError instanceof Error ? validationError.message : 'Cap nhat trang thai khong hop le');
      return;
    }

    try {
      statusUpdateInFlightRef.current = true;
      setSavingStatus(nextStatus);
      setError(null);
      const updatedShipment = await updateShipmentStatus(request, shipment.shipmentId, payload);
      setShipment(updatedShipment);
      setFailureReason(updatedShipment.failureReason ?? '');
      void refreshShipmentAfterStatusUpdate();
      Alert.alert('Cap nhat thanh cong', 'Backend da ghi nhan trang thai moi cua chuyen giao.');
    } catch (updateError) {
      setError(updateError instanceof Error ? updateError.message : 'Cap nhat trang thai that bai');
    } finally {
      statusUpdateInFlightRef.current = false;
      setSavingStatus(null);
    }
  }

  async function refreshShipmentAfterStatusUpdate() {
    if (!id) {
      return;
    }

    try {
      const refreshedShipment = await fetchShipmentById(request, id);
      setShipment(refreshedShipment);
      setFailureReason(refreshedShipment.failureReason ?? '');
    } catch {
      // The successful status response remains the server-confirmed state.
    }
  }

  function requestStatusUpdate(nextStatus: ShipmentStatus) {
    if (savingStatus || statusUpdateInFlightRef.current) {
      return;
    }

    if (!shipment) {
      return;
    }

    try {
      prepareShipmentStatusUpdate(shipment.shipmentStatus, nextStatus, failureReason);
    } catch (validationError) {
      setError(validationError instanceof Error ? validationError.message : 'Cap nhat trang thai khong hop le');
      return;
    }

    if (requiresShipmentStatusConfirmation(nextStatus)) {
      const isDelivered = nextStatus === 'DELIVERED';
      Alert.alert(
        isDelivered ? 'Xac nhan giao thanh cong' : 'Xac nhan giao that bai',
        isDelivered
          ? 'Trang thai giao thanh cong la trang thai cuoi va khong the cap nhat them.'
          : 'Trang thai giao that bai la trang thai cuoi va se luu ly do ban da nhap.',
        [
          { text: 'Huy', style: 'cancel' },
          {
            text: 'Xac nhan',
            style: 'destructive',
            onPress: () => {
              void handleStatusUpdate(nextStatus);
            },
          },
        ],
      );
      return;
    }

    void handleStatusUpdate(nextStatus);
  }

  async function openPhoneCall() {
    const phone = getCallablePhone(shipment?.receiverPhone);

    if (!phone) {
      return;
    }

    try {
      const phoneUrl = `tel:${phone}`;

      if (!(await Linking.canOpenURL(phoneUrl))) {
        throw new Error('unsupported');
      }

      await Linking.openURL(phoneUrl);
    } catch {
      setError('Thiet bi khong the mo ung dung goi dien. Vui long sao chep so dien thoai de goi.');
    }
  }

  async function openMaps() {
    const address = shipment?.receiverAddress?.trim();

    if (!address) {
      return;
    }

    try {
      const mapUrl = `https://www.google.com/maps/search/?api=1&query=${encodeURIComponent(address)}`;

      if (!(await Linking.canOpenURL(mapUrl))) {
        throw new Error('unsupported');
      }

      await Linking.openURL(mapUrl);
    } catch {
      setError('Thiet bi khong the mo ban do. Vui long sao chep dia chi de tim thu cong.');
    }
  }

  const nextActions = shipment ? getNextShipmentActions(shipment.shipmentStatus) : [];
  const canCallCustomer = Boolean(getCallablePhone(shipment?.receiverPhone));
  const canOpenMaps = Boolean(shipment?.receiverAddress?.trim());
  const canReportFailure = nextActions.some((action) => action.status === 'FAILED');

  return (
    <>
      <Stack.Screen options={{ title: shipment?.orderCode ?? 'Chi tiet chuyen' }} />
      <KeyboardAvoidingView behavior={Platform.OS === 'ios' ? 'padding' : undefined} style={styles.container}>
        <ScrollView
          contentContainerStyle={styles.content}
          keyboardShouldPersistTaps="handled"
          style={styles.container}>
        {loading ? (
          <View style={styles.loadingState}>
            <ActivityIndicator color={palette.primary} size="large" />
            <Text style={styles.loadingText}>Dang tai chi tiet chuyen...</Text>
          </View>
        ) : null}

        {!loading && error && !shipment ? (
          <Panel>
            <Text style={styles.errorTitle}>Khong tai duoc du lieu</Text>
            <Text style={styles.errorText}>{error}</Text>
            <View style={styles.singleAction}>
              <ActionButton
                icon="refresh"
                label="Thu lai"
                onPress={() => {
                  void loadShipment();
                }}
              />
            </View>
          </Panel>
        ) : null}

        {!loading && shipment ? (
          <>
            <Panel style={styles.primaryPanel}>
              <Text style={styles.orderCode}>{shipment.orderCode}</Text>
              <View style={styles.primaryRow}>
                <StatusBadge status={shipment.shipmentStatus} />
                <Text style={styles.amount}>{formatCurrency(shipment.finalAmount)}</Text>
              </View>
              <Text selectable style={styles.address}>
                {shipment.receiverAddress || 'Chua co dia chi giao hang'}
              </Text>
            </Panel>

            <Panel style={styles.quickActions}>
              <ActionButton
                disabled={!canCallCustomer}
                icon="phone-outline"
                label={canCallCustomer ? 'Goi khach' : 'Chua co so hop le'}
                onPress={() => {
                  void openPhoneCall();
                }}
                variant="soft"
              />
              <ActionButton
                disabled={!canOpenMaps}
                icon="map-marker-radius-outline"
                label={canOpenMaps ? 'Mo ban do' : 'Chua co dia chi'}
                onPress={() => {
                  void openMaps();
                }}
                variant="soft"
              />
            </Panel>

            {error ? <Text style={styles.inlineError}>{error}</Text> : null}

            <Panel style={styles.detailPanel}>
              <SectionTitle title="Nguoi nhan" />
              <View style={styles.detailRow}>
                <Text style={styles.detailLabel}>Ma don</Text>
                <Text selectable style={styles.detailValue}>{shipment.orderCode}</Text>
              </View>
              <View style={styles.detailRow}>
                <Text style={styles.detailLabel}>Ma shipment</Text>
                <Text selectable style={styles.detailValue}>{shipment.shipmentId}</Text>
              </View>
              <View style={styles.detailRow}>
                <Text style={styles.detailLabel}>Ten</Text>
                <Text selectable style={styles.detailValue}>{shipment.receiverName || 'Chua cap nhat'}</Text>
              </View>
              <View style={styles.detailRow}>
                <Text style={styles.detailLabel}>So dien thoai</Text>
                <Text selectable style={styles.detailValue}>{shipment.receiverPhone || 'Chua cap nhat'}</Text>
              </View>
              <View style={styles.detailRow}>
                <Text style={styles.detailLabel}>Dia chi</Text>
                <Text selectable style={styles.detailValue}>{shipment.receiverAddress || 'Chua cap nhat'}</Text>
              </View>
              <View style={styles.detailRow}>
                <Text style={styles.detailLabel}>Thanh toan</Text>
                <Text style={styles.detailValue}>
                  {shipment.paymentMethod} / {shipment.paymentStatus}
                </Text>
              </View>
              {shipment.paymentMethod === 'COD' ? (
                <View style={styles.detailRow}>
                  <Text style={styles.detailLabel}>Tien thu ho</Text>
                  <Text style={styles.detailValue}>{formatCurrency(shipment.finalAmount)}</Text>
                </View>
              ) : null}
              <View style={styles.detailRow}>
                <Text style={styles.detailLabel}>Don hang</Text>
                <Text style={styles.detailValue}>{shipment.orderStatus}</Text>
              </View>
            </Panel>

            <Panel style={styles.detailPanel}>
              <SectionTitle title="Moc thoi gian" />
              <View style={styles.detailRow}>
                <Text style={styles.detailLabel}>Giao ship</Text>
                <Text style={styles.detailValue}>{formatDateTime(shipment.assignedAt)}</Text>
              </View>
              <View style={styles.detailRow}>
                <Text style={styles.detailLabel}>Lay hang</Text>
                <Text style={styles.detailValue}>{formatDateTime(shipment.pickedUpAt)}</Text>
              </View>
              <View style={styles.detailRow}>
                <Text style={styles.detailLabel}>Dang giao</Text>
                <Text style={styles.detailValue}>{formatDateTime(shipment.deliveringAt)}</Text>
              </View>
              <View style={styles.detailRow}>
                <Text style={styles.detailLabel}>Hoan tat</Text>
                <Text style={styles.detailValue}>{formatDateTime(shipment.deliveredAt)}</Text>
              </View>
              <View style={styles.detailRow}>
                <Text style={styles.detailLabel}>That bai</Text>
                <Text style={styles.detailValue}>{formatDateTime(shipment.failedAt)}</Text>
              </View>
              <View style={styles.detailRow}>
                <Text style={styles.detailLabel}>Cap nhat cuoi</Text>
                <Text style={styles.detailValue}>{formatDateTime(shipment.updatedAt)}</Text>
              </View>
            </Panel>

            {nextActions.length > 0 ? (
              <Panel style={styles.detailPanel}>
                <SectionTitle
                  subtitle="Nut duoi day chi follow dung transition ma backend cho phep."
                  title="Cap nhat trang thai"
                />

                {canReportFailure ? (
                  <View style={styles.failureBox}>
                    <Text style={styles.failureLabel}>Ly do that bai</Text>
                    <TextInput
                      multiline
                      numberOfLines={3}
                      onChangeText={setFailureReason}
                      placeholder="Bat buoc khi bao giao that bai"
                      placeholderTextColor={palette.textMuted}
                      style={styles.failureInput}
                      textAlignVertical="top"
                      value={failureReason}
                    />
                  </View>
                ) : null}

                <View style={styles.actionList}>
                  {nextActions.map((action) => (
                    <ActionButton
                      key={action.status}
                      icon={action.icon}
                      label={action.label}
                      disabled={Boolean(savingStatus)}
                      loading={savingStatus === action.status}
                      onPress={() => {
                        requestStatusUpdate(action.status);
                      }}
                      tone={action.status === 'FAILED' ? palette.danger : palette.primary}
                    />
                  ))}
                </View>
              </Panel>
            ) : (
              <Panel style={styles.detailPanel}>
                <Text style={styles.doneTitle}>Chuyen nay da ket thuc</Text>
                <Text style={styles.doneText}>
                  Backend se khong cho cap nhat them vi trang thai da o trang thai cuoi.
                </Text>
                {shipment.failureReason ? (
                  <View style={styles.failureNote}>
                    <MaterialCommunityIcons color={palette.danger} name="alert-circle-outline" size={18} />
                    <Text style={styles.failureNoteText}>{shipment.failureReason}</Text>
                  </View>
                ) : null}
              </Panel>
            )}
          </>
        ) : null}
        </ScrollView>
      </KeyboardAvoidingView>
    </>
  );
}

function getCallablePhone(phone: string | null | undefined) {
  const normalizedPhone = phone?.trim().replace(/[\s().-]/g, '') ?? '';

  return /^\+?[0-9]{7,15}$/.test(normalizedPhone) ? normalizedPhone : null;
}

const styles = StyleSheet.create({
  container: {
    backgroundColor: palette.background,
    flex: 1,
  },
  content: {
    gap: 16,
    padding: 18,
    paddingBottom: 32,
  },
  loadingState: {
    alignItems: 'center',
    gap: 12,
    justifyContent: 'center',
    minHeight: 320,
  },
  loadingText: {
    color: palette.textMuted,
    fontSize: 14,
    fontWeight: '600',
  },
  primaryPanel: {
    backgroundColor: '#102033',
    borderColor: '#1E3A5F',
    gap: 14,
  },
  orderCode: {
    color: '#FFFFFF',
    fontSize: 30,
    fontWeight: '900',
  },
  primaryRow: {
    alignItems: 'center',
    flexDirection: 'row',
    gap: 12,
    justifyContent: 'space-between',
  },
  amount: {
    color: '#F8FAFC',
    fontSize: 20,
    fontWeight: '800',
  },
  address: {
    color: '#CBD5E1',
    fontSize: 15,
    lineHeight: 24,
  },
  quickActions: {
    flexDirection: 'row',
    gap: 12,
  },
  detailPanel: {
    gap: 14,
  },
  detailRow: {
    flexDirection: 'row',
    gap: 12,
    justifyContent: 'space-between',
  },
  detailLabel: {
    color: palette.textMuted,
    flex: 1,
    fontSize: 14,
    fontWeight: '700',
  },
  detailValue: {
    color: palette.text,
    flex: 1.3,
    fontSize: 14,
    lineHeight: 22,
    textAlign: 'right',
  },
  failureBox: {
    gap: 8,
  },
  failureLabel: {
    color: palette.text,
    fontSize: 14,
    fontWeight: '700',
  },
  failureInput: {
    backgroundColor: palette.surfaceMuted,
    borderColor: palette.border,
    borderRadius: 14,
    borderWidth: 1,
    color: palette.text,
    fontSize: 15,
    minHeight: 96,
    padding: 14,
  },
  inlineError: {
    color: palette.danger,
    fontSize: 14,
    fontWeight: '600',
    lineHeight: 20,
  },
  actionList: {
    gap: 10,
  },
  doneTitle: {
    color: palette.text,
    fontSize: 18,
    fontWeight: '800',
  },
  doneText: {
    color: palette.textMuted,
    fontSize: 14,
    lineHeight: 22,
  },
  failureNote: {
    alignItems: 'center',
    backgroundColor: '#FEE2E2',
    borderRadius: 14,
    flexDirection: 'row',
    gap: 10,
    padding: 14,
  },
  failureNoteText: {
    color: palette.danger,
    flex: 1,
    fontSize: 14,
    fontWeight: '700',
    lineHeight: 22,
  },
  errorTitle: {
    color: palette.danger,
    fontSize: 18,
    fontWeight: '800',
  },
  errorText: {
    color: palette.text,
    fontSize: 14,
    lineHeight: 22,
  },
  singleAction: {
    marginTop: 12,
  },
});

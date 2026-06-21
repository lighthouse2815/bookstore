import { palette } from '@/src/config';
import { formatCurrency, formatDateTime } from '@/src/lib/format';
import { getNextShipmentActions } from '@/src/lib/status';
import { fetchShipmentById, updateShipmentStatus } from '@/src/services/shipments';
import type { Shipment, ShipmentStatus } from '@/src/types/shipment';
import { ActionButton, Panel, SectionTitle, StatusBadge } from '@/src/components/ui';
import { useSession } from '@/src/context/session-context';
import MaterialCommunityIcons from '@expo/vector-icons/MaterialCommunityIcons';
import { Redirect, Stack, useLocalSearchParams } from 'expo-router';
import { useEffect, useState } from 'react';
import {
  ActivityIndicator,
  Alert,
  Linking,
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
    if (!shipment) {
      return;
    }

    if (nextStatus === 'FAILED' && !failureReason.trim()) {
      setError('Nhap ly do that bai truoc khi gui cap nhat');
      return;
    }

    try {
      setSavingStatus(nextStatus);
      setError(null);
      const updatedShipment = await updateShipmentStatus(request, shipment.shipmentId, {
        status: nextStatus,
        failureReason: nextStatus === 'FAILED' ? failureReason.trim() : null,
      });
      setShipment(updatedShipment);
      setFailureReason(updatedShipment.failureReason ?? '');
      Alert.alert('Cap nhat thanh cong', 'Backend da ghi nhan trang thai moi cua chuyen giao.');
    } catch (updateError) {
      setError(updateError instanceof Error ? updateError.message : 'Cap nhat trang thai that bai');
    } finally {
      setSavingStatus(null);
    }
  }

  function openPhoneCall() {
    if (!shipment?.receiverPhone) {
      return;
    }

    void Linking.openURL(`tel:${shipment.receiverPhone}`);
  }

  function openMaps() {
    if (!shipment?.receiverAddress) {
      return;
    }

    const encodedAddress = encodeURIComponent(shipment.receiverAddress);
    void Linking.openURL(`https://www.google.com/maps/search/?api=1&query=${encodedAddress}`);
  }

  const nextActions = shipment ? getNextShipmentActions(shipment.shipmentStatus) : [];

  return (
    <>
      <Stack.Screen options={{ title: shipment?.orderCode ?? 'Chi tiet chuyen' }} />
      <ScrollView contentContainerStyle={styles.content} style={styles.container}>
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
              <Text style={styles.address}>{shipment.receiverAddress}</Text>
            </Panel>

            <Panel style={styles.quickActions}>
              <ActionButton icon="phone-outline" label="Goi khach" onPress={openPhoneCall} variant="soft" />
              <ActionButton icon="map-marker-radius-outline" label="Mo ban do" onPress={openMaps} variant="soft" />
            </Panel>

            <Panel style={styles.detailPanel}>
              <SectionTitle title="Nguoi nhan" />
              <View style={styles.detailRow}>
                <Text style={styles.detailLabel}>Ten</Text>
                <Text style={styles.detailValue}>{shipment.receiverName}</Text>
              </View>
              <View style={styles.detailRow}>
                <Text style={styles.detailLabel}>So dien thoai</Text>
                <Text style={styles.detailValue}>{shipment.receiverPhone}</Text>
              </View>
              <View style={styles.detailRow}>
                <Text style={styles.detailLabel}>Thanh toan</Text>
                <Text style={styles.detailValue}>
                  {shipment.paymentMethod} / {shipment.paymentStatus}
                </Text>
              </View>
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

                <View style={styles.failureBox}>
                  <Text style={styles.failureLabel}>Ly do that bai</Text>
                  <TextInput
                    multiline
                    numberOfLines={3}
                    onChangeText={setFailureReason}
                    placeholder="Chi nhap khi can bao giao that bai"
                    placeholderTextColor={palette.textMuted}
                    style={styles.failureInput}
                    textAlignVertical="top"
                    value={failureReason}
                  />
                </View>

                {error ? <Text style={styles.inlineError}>{error}</Text> : null}

                <View style={styles.actionList}>
                  {nextActions.map((action) => (
                    <ActionButton
                      key={action.status}
                      icon={action.icon}
                      label={action.label}
                      loading={savingStatus === action.status}
                      onPress={() => {
                        void handleStatusUpdate(action.status);
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
    </>
  );
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

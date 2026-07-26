import { palette } from '@/src/config';
import { shipmentStatusMeta } from '@/src/lib/status';
import { fetchMyShipments } from '@/src/services/shipments';
import type { Shipment, ShipmentFilter } from '@/src/types/shipment';
import { ActionButton, MetricCard, Panel, SectionTitle, ShipmentCard } from '@/src/components/ui';
import { useSession } from '@/src/context/session-context';
import MaterialCommunityIcons from '@expo/vector-icons/MaterialCommunityIcons';
import { Redirect, router, useIsFocused } from 'expo-router';
import { useEffect, useState } from 'react';
import {
  ActivityIndicator,
  Alert,
  Pressable,
  RefreshControl,
  ScrollView,
  StyleSheet,
  Text,
  View,
} from 'react-native';

const filters: ShipmentFilter[] = ['ALL', 'ASSIGNED', 'PICKED_UP', 'DELIVERING', 'DELIVERED', 'FAILED'];

export default function ShipmentsScreen() {
  const { hydrated, request, session, signOut } = useSession();
  const isFocused = useIsFocused();
  const [shipments, setShipments] = useState<Shipment[]>([]);
  const [filter, setFilter] = useState<ShipmentFilter>('ALL');
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!hydrated || !session || !isFocused) {
      return;
    }

    void loadShipments(false);
  }, [hydrated, isFocused, session]);

  if (hydrated && !session) {
    return <Redirect href="/login" />;
  }

  async function loadShipments(isRefresh: boolean) {
    try {
      setError(null);
      if (isRefresh) {
        setRefreshing(true);
      } else {
        setLoading(true);
      }

      const nextShipments = await fetchMyShipments(request);
      setShipments(nextShipments);
    } catch (loadError) {
      setError(loadError instanceof Error ? loadError.message : 'Khong tai duoc danh sach chuyyen');
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  }

  const filteredShipments =
    filter === 'ALL'
      ? shipments
      : shipments.filter((shipment) => shipment.shipmentStatus === filter);

  const deliveringCount = shipments.filter((shipment) =>
    shipment.shipmentStatus === 'PICKED_UP' || shipment.shipmentStatus === 'DELIVERING',
  ).length;
  const deliveredCount = shipments.filter((shipment) => shipment.shipmentStatus === 'DELIVERED').length;
  const failedCount = shipments.filter((shipment) => shipment.shipmentStatus === 'FAILED').length;

  function confirmLogout() {
    Alert.alert('Dang xuat', 'Ket thuc phien shipper tren thiet bi nay?', [
      { text: 'Huy', style: 'cancel' },
      {
        text: 'Dang xuat',
        style: 'destructive',
        onPress: () => {
          void signOut();
        },
      },
    ]);
  }

  return (
    <ScrollView
      contentContainerStyle={styles.content}
      refreshControl={
        <RefreshControl
          onRefresh={() => {
            void loadShipments(true);
          }}
          refreshing={refreshing}
          tintColor={palette.primary}
        />
      }
      style={styles.container}>
      <Panel style={styles.heroPanel}>
        <View style={styles.heroHeader}>
          <View style={styles.heroTextBlock}>
            <Text style={styles.heroEyebrow}>Ca giao hien tai</Text>
            <Text style={styles.heroTitle}>Quan ly chuyyen giao sach</Text>
            <Text style={styles.heroSubtitle}>
              App nay dung rieng cho shipper. Danh sach duoi day chi lay tu `GET /api/shipper/shipments/my`.
            </Text>
          </View>
          <View style={styles.heroActions}>
            <ActionButton
              icon="refresh"
              label="Tai lai"
              onPress={() => {
                void loadShipments(true);
              }}
              variant="soft"
            />
            <ActionButton icon="logout" label="Dang xuat" onPress={confirmLogout} variant="ghost" />
          </View>
        </View>

        <View style={styles.metricsRow}>
          <MetricCard label="Tong chuyen" tone={palette.primary} value={`${shipments.length}`} />
          <MetricCard label="Dang xu ly" tone={palette.accent} value={`${deliveringCount}`} />
          <MetricCard label="Hoan tat" tone={palette.success} value={`${deliveredCount}`} />
          <MetricCard label="That bai" tone={palette.danger} value={`${failedCount}`} />
        </View>
      </Panel>

      <SectionTitle
        subtitle="Bam vao tung chuyen de xem chi tiet, goi khach, mo ban do, va cap nhat trang thai."
        title="Danh sach chuyen"
      />

      <ScrollView
        horizontal
        showsHorizontalScrollIndicator={false}
        contentContainerStyle={styles.filterRow}>
        {filters.map((item) => {
          const isActive = filter === item;
          const color = item === 'ALL' ? palette.primaryDark : shipmentStatusMeta[item].tone;

          return (
            <Pressable
              key={item}
              onPress={() => setFilter(item)}
              style={[
                styles.filterChip,
                {
                  backgroundColor: isActive ? color : palette.surface,
                  borderColor: color,
                },
              ]}>
              {item === 'ALL' ? (
                <MaterialCommunityIcons
                  color={isActive ? '#FFFFFF' : palette.primaryDark}
                  name="view-grid-outline"
                  size={16}
                />
              ) : (
                <MaterialCommunityIcons
                  color={isActive ? '#FFFFFF' : color}
                  name={shipmentStatusMeta[item].icon as never}
                  size={16}
                />
              )}
              <Text style={[styles.filterLabel, { color: isActive ? '#FFFFFF' : color }]}>
                {item === 'ALL' ? 'Tat ca' : shipmentStatusMeta[item].label}
              </Text>
            </Pressable>
          );
        })}
      </ScrollView>

      {loading ? (
        <View style={styles.loadingState}>
          <ActivityIndicator color={palette.primary} size="large" />
          <Text style={styles.loadingText}>Dang tai danh sach chuyen...</Text>
        </View>
      ) : null}

      {!loading && error ? (
        <Panel style={styles.errorPanel}>
          <Text style={styles.errorTitle}>Khong tai duoc du lieu</Text>
          <Text style={styles.errorText}>{error}</Text>
          <View style={styles.errorAction}>
            <ActionButton
              icon="refresh"
              label="Thu lai"
              onPress={() => {
                void loadShipments(true);
              }}
            />
          </View>
        </Panel>
      ) : null}

      {!loading && !error && filteredShipments.length === 0 ? (
        <Panel>
          <Text style={styles.emptyTitle}>Khong co chuyen phu hop bo loc</Text>
          <Text style={styles.emptyText}>
            Neu backend da giao don cho shipper nay, keo xuong de tai lai danh sach.
          </Text>
        </Panel>
      ) : null}

      {!loading && !error ? (
        <View style={styles.list}>
          {filteredShipments.map((shipment) => (
            <ShipmentCard
              key={shipment.shipmentId}
              onPress={() => router.push(`/shipments/${shipment.shipmentId}`)}
              shipment={shipment}
            />
          ))}
        </View>
      ) : null}
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: {
    backgroundColor: palette.background,
    flex: 1,
  },
  content: {
    gap: 18,
    padding: 18,
    paddingBottom: 32,
  },
  heroPanel: {
    backgroundColor: '#0F172A',
    borderColor: '#1E293B',
    gap: 20,
  },
  heroHeader: {
    gap: 16,
  },
  heroTextBlock: {
    gap: 8,
  },
  heroEyebrow: {
    color: '#7DD3FC',
    fontSize: 12,
    fontWeight: '700',
    textTransform: 'uppercase',
  },
  heroTitle: {
    color: '#FFFFFF',
    fontSize: 28,
    fontWeight: '900',
    lineHeight: 34,
  },
  heroSubtitle: {
    color: '#CBD5E1',
    fontSize: 14,
    lineHeight: 22,
  },
  heroActions: {
    flexDirection: 'row',
    gap: 12,
  },
  metricsRow: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 12,
  },
  filterRow: {
    gap: 10,
  },
  filterChip: {
    alignItems: 'center',
    borderRadius: 999,
    borderWidth: 1,
    flexDirection: 'row',
    gap: 6,
    minHeight: 42,
    paddingHorizontal: 14,
  },
  filterLabel: {
    fontSize: 13,
    fontWeight: '700',
  },
  loadingState: {
    alignItems: 'center',
    gap: 12,
    paddingVertical: 48,
  },
  loadingText: {
    color: palette.textMuted,
    fontSize: 14,
    fontWeight: '600',
  },
  errorPanel: {
    gap: 8,
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
  errorAction: {
    marginTop: 8,
  },
  emptyTitle: {
    color: palette.text,
    fontSize: 18,
    fontWeight: '800',
  },
  emptyText: {
    color: palette.textMuted,
    fontSize: 14,
    lineHeight: 22,
    marginTop: 8,
  },
  list: {
    gap: 14,
  },
});

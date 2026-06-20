import { palette } from '@/src/config';
import { formatCurrency, formatDateTime } from '@/src/lib/format';
import { shipmentStatusMeta } from '@/src/lib/status';
import type { Shipment } from '@/src/types/shipment';
import MaterialCommunityIcons from '@expo/vector-icons/MaterialCommunityIcons';
import type { ReactNode } from 'react';
import {
  ActivityIndicator,
  Pressable,
  StyleSheet,
  type StyleProp,
  Text,
  type TextStyle,
  View,
  type ViewStyle,
} from 'react-native';

export function Panel({
  children,
  style,
}: {
  children: ReactNode;
  style?: StyleProp<ViewStyle>;
}) {
  return <View style={[styles.panel, style]}>{children}</View>;
}

export function SectionTitle({ title, subtitle }: { title: string; subtitle?: string }) {
  return (
    <View style={styles.sectionHeader}>
      <Text style={styles.sectionTitle}>{title}</Text>
      {subtitle ? <Text style={styles.sectionSubtitle}>{subtitle}</Text> : null}
    </View>
  );
}

export function StatusBadge({ status }: { status: Shipment['shipmentStatus'] }) {
  const meta = shipmentStatusMeta[status];

  return (
    <View style={[styles.statusBadge, { backgroundColor: meta.soft }]}>
      <MaterialCommunityIcons color={meta.tone} name={meta.icon as never} size={16} />
      <Text style={[styles.statusLabel, { color: meta.tone }]}>{meta.label}</Text>
    </View>
  );
}

export function MetricCard({
  label,
  value,
  tone,
}: {
  label: string;
  value: string;
  tone: string;
}) {
  return (
    <View style={[styles.metricCard, { borderColor: tone }]}>
      <Text style={styles.metricLabel}>{label}</Text>
      <Text style={[styles.metricValue, { color: tone }]}>{value}</Text>
    </View>
  );
}

export function ActionButton({
  label,
  icon,
  onPress,
  tone = palette.primary,
  variant = 'solid',
  disabled,
  loading,
  textStyle,
}: {
  label: string;
  icon: string;
  onPress: () => void;
  tone?: string;
  variant?: 'solid' | 'soft' | 'ghost';
  disabled?: boolean;
  loading?: boolean;
  textStyle?: TextStyle;
}) {
  const backgroundColor =
    variant === 'solid' ? tone : variant === 'soft' ? `${tone}18` : 'transparent';
  const borderWidth = variant === 'ghost' ? 1 : 0;

  return (
    <Pressable
      disabled={disabled || loading}
      onPress={onPress}
      style={({ pressed }) => [
        styles.button,
        {
          backgroundColor,
          borderColor: tone,
          borderWidth,
          opacity: pressed || disabled ? 0.74 : 1,
        },
      ]}>
      {loading ? (
        <ActivityIndicator color={variant === 'solid' ? '#FFFFFF' : tone} size="small" />
      ) : (
        <MaterialCommunityIcons
          color={variant === 'solid' ? '#FFFFFF' : tone}
          name={icon as never}
          size={18}
        />
      )}
      <Text
        style={[
          styles.buttonLabel,
          { color: variant === 'solid' ? '#FFFFFF' : tone },
          textStyle,
        ]}>
        {label}
      </Text>
    </Pressable>
  );
}

export function ShipmentCard({
  shipment,
  onPress,
}: {
  shipment: Shipment;
  onPress: () => void;
}) {
  return (
    <Pressable
      onPress={onPress}
      style={({ pressed }) => [styles.shipmentCard, pressed && styles.cardPressed]}>
      <View style={styles.shipmentTopRow}>
        <View style={styles.shipmentHeaderBlock}>
          <Text style={styles.orderCode}>{shipment.orderCode}</Text>
          <Text style={styles.receiverName}>{shipment.receiverName}</Text>
        </View>
        <StatusBadge status={shipment.shipmentStatus} />
      </View>

      <View style={styles.metaRow}>
        <MaterialCommunityIcons color={palette.textMuted} name="map-marker-outline" size={18} />
        <Text numberOfLines={2} style={styles.metaText}>
          {shipment.receiverAddress}
        </Text>
      </View>

      <View style={styles.metaRow}>
        <MaterialCommunityIcons color={palette.textMuted} name="cash-multiple" size={18} />
        <Text style={styles.metaText}>{formatCurrency(shipment.finalAmount)}</Text>
      </View>

      <View style={styles.metaRow}>
        <MaterialCommunityIcons color={palette.textMuted} name="clock-outline" size={18} />
        <Text style={styles.metaText}>{formatDateTime(shipment.updatedAt)}</Text>
      </View>

      <View style={styles.shipmentFooter}>
        <Text style={styles.footerHint}>Mo chi tiet va cap nhat trang thai</Text>
        <MaterialCommunityIcons color={palette.text} name="chevron-right" size={20} />
      </View>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  panel: {
    backgroundColor: palette.surface,
    borderRadius: 18,
    borderWidth: 1,
    borderColor: palette.border,
    padding: 18,
    shadowColor: palette.shadow,
    shadowOffset: { width: 0, height: 10 },
    shadowOpacity: 1,
    shadowRadius: 20,
    elevation: 3,
  },
  sectionHeader: {
    gap: 4,
  },
  sectionTitle: {
    color: palette.text,
    fontSize: 20,
    fontWeight: '800',
  },
  sectionSubtitle: {
    color: palette.textMuted,
    fontSize: 13,
    lineHeight: 20,
  },
  statusBadge: {
    alignItems: 'center',
    alignSelf: 'flex-start',
    borderRadius: 999,
    flexDirection: 'row',
    gap: 6,
    paddingHorizontal: 10,
    paddingVertical: 7,
  },
  statusLabel: {
    fontSize: 12,
    fontWeight: '700',
  },
  metricCard: {
    backgroundColor: palette.surface,
    borderRadius: 18,
    borderWidth: 1,
    flex: 1,
    minHeight: 92,
    padding: 16,
  },
  metricLabel: {
    color: palette.textMuted,
    fontSize: 13,
  },
  metricValue: {
    fontSize: 24,
    fontWeight: '800',
    marginTop: 12,
  },
  button: {
    alignItems: 'center',
    borderRadius: 14,
    flexDirection: 'row',
    gap: 8,
    justifyContent: 'center',
    minHeight: 48,
    paddingHorizontal: 16,
    paddingVertical: 12,
  },
  buttonLabel: {
    fontSize: 15,
    fontWeight: '700',
  },
  shipmentCard: {
    backgroundColor: palette.surface,
    borderColor: palette.border,
    borderRadius: 18,
    borderWidth: 1,
    gap: 12,
    padding: 16,
  },
  cardPressed: {
    opacity: 0.82,
  },
  shipmentTopRow: {
    alignItems: 'flex-start',
    flexDirection: 'row',
    gap: 12,
    justifyContent: 'space-between',
  },
  shipmentHeaderBlock: {
    flex: 1,
    gap: 4,
  },
  orderCode: {
    color: palette.text,
    fontSize: 18,
    fontWeight: '800',
  },
  receiverName: {
    color: palette.textMuted,
    fontSize: 14,
    fontWeight: '600',
  },
  metaRow: {
    alignItems: 'center',
    flexDirection: 'row',
    gap: 10,
  },
  metaText: {
    color: palette.text,
    flex: 1,
    fontSize: 14,
    lineHeight: 20,
  },
  shipmentFooter: {
    alignItems: 'center',
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginTop: 4,
  },
  footerHint: {
    color: palette.textMuted,
    fontSize: 12,
    fontWeight: '600',
  },
});

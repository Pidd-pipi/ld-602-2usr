<script setup lang="ts">
import { onMounted } from "vue";
import { useShelterStore } from "../stores/ShelterStore";
import { useDispatchOrderStore } from "../stores/DispatchOrderStore";
import CapacityMeter from "../components/common/CapacityMeter.vue";
import StatusBadge from "../components/common/StatusBadge.vue";
import EmptyState from "../components/common/EmptyState.vue";
import { formatRisk } from "../utils/formatters";

const shelterStore = useShelterStore();
const dispatchStore = useDispatchOrderStore();

onMounted(async () => {
  await Promise.all([shelterStore.load(), dispatchStore.load()]);
});

function inboundCount(shelterId: number) {
  return dispatchStore.rows.filter((o) => o.shelter_id === shelterId).length;
}
</script>

<template>
  <section class="shelters-page">
    <div class="page-head">
      <div>
        <p class="eyebrow">SHELTERS · 安置与接收</p>
        <h2>避难点管理</h2>
      </div>
    </div>

    <div class="grid">
      <article v-for="s in shelterStore.rows" :key="s.id" class="card">
        <div class="card-head">
          <strong>{{ s.name }}</strong>
          <StatusBadge :value="s.open_status" />
        </div>
        <p class="muted">{{ s.district }} · 联系人 {{ s.contact_person }} · {{ formatRisk(s.risk_level) }}风险</p>
        <CapacityMeter :capacity="s.capacity" :current="s.current_population" />
        <p class="inbound">关联调拨单 <b>{{ inboundCount(s.id) }}</b> 单</p>
      </article>
    </div>
    <EmptyState v-if="!shelterStore.rows.length && !shelterStore.loading" text="暂无避难点数据" />
  </section>
</template>

<style scoped>
.shelters-page { display: grid; gap: 16px; }
.eyebrow { margin: 0; color: #7d4d18; font-size: 11px; font-weight: 800; letter-spacing: .08em; }
h2 { margin: 4px 0; font-size: 24px; }
.grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 14px; }
.card { background: #fff; border: 1px solid #d8d6c8; border-radius: 10px; padding: 18px; display: grid; gap: 10px; }
.card-head { display: flex; justify-content: space-between; align-items: center; }
.muted { color: #7c837b; font-size: 13px; margin: 0; }
.inbound { font-size: 13px; color: #596257; margin: 4px 0 0; }
</style>

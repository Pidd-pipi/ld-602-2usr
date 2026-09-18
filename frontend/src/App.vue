<script setup lang="ts">
import { computed, ref, type Component } from "vue";
import { routes } from "./router/routes";
import StatusBadge from "./components/common/StatusBadge.vue";
import DashboardPage from "./pages/DashboardPage.vue";
import WarehousesPage from "./pages/WarehousesPage.vue";
import SheltersPage from "./pages/SheltersPage.vue";
import DispatchPage from "./pages/DispatchPage.vue";
import EventsPage from "./pages/EventsPage.vue";

const pages: Record<string, Component> = {
  "/dashboard": DashboardPage,
  "/warehouses": WarehousesPage,
  "/shelters": SheltersPage,
  "/dispatch": DispatchPage,
  "/events": EventsPage
};

const active = ref<string>("/dispatch");
const current = computed(() => routes.find((route) => route.route === active.value) ?? routes[0]);
const pageComponent = computed(() => pages[active.value] ?? DashboardPage);
</script>

<template>
  <div class="shell">
    <aside>
      <div class="brand">城市防灾应急物资调度系统</div>
      <nav>
        <button v-for="route in routes" :key="route.route" :class="{ active: active === route.route }" @click="active = route.route">{{ route.name }}</button>
      </nav>
    </aside>
    <main class="page">
      <section class="page-head">
        <div><p class="eyebrow">rescue-stock</p><h1>{{ current?.name }}</h1></div>
        <StatusBadge value="LOCAL_DATA" />
      </section>
      <component :is="pageComponent" />
    </main>
  </div>
</template>

import { defineStore } from "pinia";
import { listShelter } from "../api/Shelter";
import type { Shelter } from "../types/Shelter";

export const useShelterStore = defineStore("shelter", {
  state: () => ({ rows: [] as Shelter[], loading: false }),
  actions: {
    async load() {
      this.loading = true;
      try {
        this.rows = await listShelter();
      } finally {
        this.loading = false;
      }
    }
  }
});

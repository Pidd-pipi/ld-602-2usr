import type { DispatchEvent } from "../types/DispatchEvent";

export const createDefaultDispatchEvent = (overrides: Partial<DispatchEvent> = {}): DispatchEvent => ({
  id: 0,
  name: "",
  disaster_type: "",
  level: "LEVEL_IV",
  status: "RESPONDING",
  occurred_at: "",
  description: "",
  orders: [],
  ...overrides
});

export const createDispatchEventResponse = createDefaultDispatchEvent;

import { getJson } from "./request";
import type { DisasterEvent, DisasterEventDetail } from "../types/DisasterEvent";

const endpoint = "/api/event";

export async function listDisasterEvent(): Promise<DisasterEvent[]> {
  return getJson<DisasterEvent[]>(endpoint);
}

export async function getDisasterEvent(id: number): Promise<DisasterEventDetail> {
  return getJson<DisasterEventDetail>(`${endpoint}/${id}`);
}

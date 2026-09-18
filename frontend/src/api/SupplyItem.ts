import { mockData } from "../mocks/seedData";
import { getJson, ApiError } from "./request";
import type { SupplyItem } from "../types/SupplyItem";

const endpoint = "/api/supply-item";

export async function listSupplyItem(): Promise<SupplyItem[]> {
  try {
    return await getJson<SupplyItem[]>(endpoint);
  } catch (err) {
    if (err instanceof ApiError) throw err;
    return [...(mockData.supplyItem as unknown as SupplyItem[])];
  }
}

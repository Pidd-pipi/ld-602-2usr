import { mockData } from "../mocks/seedData";
import { getJson, ApiError } from "./request";
import type { Warehouse } from "../types/Warehouse";

const endpoint = "/api/warehouse";

export async function listWarehouse(): Promise<Warehouse[]> {
  try {
    return await getJson<Warehouse[]>(endpoint);
  } catch (err) {
    if (err instanceof ApiError) throw err;
    return [...(mockData.warehouse as unknown as Warehouse[])];
  }
}

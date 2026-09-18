import { mockData } from "../mocks/seedData";
import { getJson, ApiError } from "./request";
import type { Shelter } from "../types/Shelter";

const endpoint = "/api/shelter";

export async function listShelter(): Promise<Shelter[]> {
  try {
    return await getJson<Shelter[]>(endpoint);
  } catch (err) {
    if (err instanceof ApiError) throw err;
    return [...(mockData.shelter as unknown as Shelter[])];
  }
}

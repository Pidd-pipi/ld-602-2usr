import { ERROR_MESSAGES } from "../constants/errorMessages";

export interface ApiErrorBody {
  code: string;
  message: string;
  shortages?: unknown;
}

/** 业务异常：保留错误码（如 RESERVATION_SHORTAGE）与缺口明细，页面据此提示 */
export class ApiError extends Error {
  code: string;
  status: number;
  shortages?: unknown;

  constructor(body: ApiErrorBody | null, status: number) {
    super(body?.message || ERROR_MESSAGES.INTERNAL_ERROR);
    this.name = "ApiError";
    this.code = body?.code || "INTERNAL_ERROR";
    this.status = status;
    this.shortages = body?.shortages;
  }
}

type Json = Record<string, unknown> | unknown[] | undefined;

export async function getJson<T>(url: string): Promise<T> {
  const res = await fetch(url, { headers: { Accept: "application/json" } });
  if (!res.ok) {
    throw new ApiError(await safeJson(res), res.status);
  }
  return (await res.json()) as T;
}

export async function postJson<T>(url: string, body: unknown): Promise<T> {
  const res = await fetch(url, {
    method: "POST",
    headers: { "Content-Type": "application/json", Accept: "application/json" },
    body: body === undefined ? "{}" : JSON.stringify(body)
  });
  if (!res.ok) {
    throw new ApiError(await safeJson(res), res.status);
  }
  return (await res.json()) as T;
}

async function safeJson(res: Response): Promise<ApiErrorBody | null> {
  try {
    return (await res.json()) as ApiErrorBody;
  } catch {
    return null;
  }
}

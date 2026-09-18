import type { StockShortage } from "../types/StockShortage";

// 统一请求封装：非 2xx 时解析后端 {code,message,details} 并抛出 ApiError。
export class ApiError extends Error {
  code: string;
  status: number;
  shortages: StockShortage[];

  constructor(status: number, code: string, message: string, shortages: StockShortage[] = []) {
    super(message);
    this.status = status;
    this.code = code;
    this.shortages = shortages;
  }
}

interface ErrorBody {
  code?: string;
  message?: string;
  details?: { shortages?: StockShortage[] } | null;
}

async function parseError(res: Response): Promise<ApiError> {
  let body: ErrorBody = {};
  try {
    body = (await res.json()) as ErrorBody;
  } catch {
    // 非 JSON 错误响应，走默认消息。
  }
  return new ApiError(res.status, body.code ?? "INTERNAL_ERROR", body.message ?? `请求失败（HTTP ${res.status}）`,
    body.details?.shortages ?? []);
}

export async function request<T>(endpoint: string, init?: RequestInit): Promise<T> {
  const res = await fetch(endpoint, init);
  if (!res.ok) {
    throw await parseError(res);
  }
  return (await res.json()) as T;
}

export async function postJson<T>(endpoint: string, payload?: unknown): Promise<T> {
  return request<T>(endpoint, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload ?? {})
  });
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T | null;
  timestamp: string;
}

export interface ApiResult<T> {
  ok: boolean;
  status: number;
  body: ApiResponse<T> | null;
}

export interface RequestOptions extends Omit<RequestInit, 'body'> {
  json?: unknown;
}

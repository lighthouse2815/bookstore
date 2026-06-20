export interface Session {
  userId: string;
  status: string;
  roles: string[];
  accessToken: string;
  refreshToken: string;
}

export interface LoginPayload {
  username: string;
  password: string;
}

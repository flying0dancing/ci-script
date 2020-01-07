export interface User {
  id: number;
  username: string;
}

export type GetUsersResponse = User[];

export interface GetCurrentUserResponse {
  User;
  _links: {
    self: {
      href: string;
    };
  };
}

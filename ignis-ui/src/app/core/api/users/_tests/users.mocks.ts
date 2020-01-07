import { GetUsersResponse, User } from "../users.interfaces";

export const username = "test";
export const newPassword = "passwordNew";
export const oldPassword = "passwordOld";
export const user: User = { username: username, id: 1 };

export const getUsersResponse: GetUsersResponse = [user];

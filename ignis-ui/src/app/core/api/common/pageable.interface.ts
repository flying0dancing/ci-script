export interface Page {
  number: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface Pageable {
  page?: number;
  size?: number;
  sort?: Sort;
}

export interface Sort {
  property: string;
  direction: string;
}

export function toUrlParams(page: Pageable): string {
  const urlParams = [];

  if (page.page) {
    urlParams.push(`page=${page.page}`);
  }

  if (page.size) {
    urlParams.push(`size=${page.size}`);
  }

  if (page.sort) {
    urlParams.push(`sort=${page.sort.property},${page.sort.direction}`)
  }

  return urlParams.join('&');
}

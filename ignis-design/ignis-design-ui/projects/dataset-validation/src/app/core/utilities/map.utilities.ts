type K = string | number;

export function mapToObject<V>(map: Map<K, V>): { [key: number]: V } {
  const obj = Object.create(null);
  map.forEach((value, key) => {
    obj[key] = value;
  });

  return obj;
}

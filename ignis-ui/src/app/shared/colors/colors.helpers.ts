export const randomColor = () => {
  const chars = "1234567890ABCDEF";
  let color = "";

  for (let i = 0; i < 6; i++) {
    color += chars[Math.floor(Math.random() * chars.length)];
  }

  return `#${color}`;
};

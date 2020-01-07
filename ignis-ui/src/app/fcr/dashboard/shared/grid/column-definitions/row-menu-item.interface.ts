export interface RowMenuItem {
  icon?: string;
  label?: string;
  disabled?: boolean;

  onClick(): void;
}

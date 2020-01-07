import { animate, style, transition, trigger } from "@angular/animations";

export const GRID_FADE_IN = trigger("gridFadeIn", [
  transition(":enter", [
    style({ opacity: 0 }),
    animate("500ms 500ms ease-in-out", style({ opacity: 1 }))
  ])
]);

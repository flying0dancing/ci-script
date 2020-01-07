import { animate, style, transition, trigger } from "@angular/animations";

export function fadeInFactory(
  triggerName: string,
  duration: string = "500ms",
  easing: string = "ease-in-out"
) {
  return trigger(`${triggerName}FadeIn`, [
    transition(":enter", [
      style({ opacity: 0 }),
      animate(`${duration} ${easing}`, style({ opacity: 1 }))
    ])
  ]);
}

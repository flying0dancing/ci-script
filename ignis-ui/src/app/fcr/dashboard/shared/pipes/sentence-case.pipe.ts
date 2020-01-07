import { Pipe, PipeTransform } from "@angular/core";

@Pipe({
  name: "sentenceCase"
})
export class SentenceCasePipe implements PipeTransform {
  transform(value: string): string {
    if (typeof value !== "string") {
      return value;
    }

    return value.charAt(0).toUpperCase() + value.slice(1);
  }
}

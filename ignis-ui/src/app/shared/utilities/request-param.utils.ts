export class RequestParamBuilder {
  private paramMap: Map<string, string> = new Map<string, string>();

  public param(key: string, value: any): RequestParamBuilder {
    this.paramMap.set(key, value);
    return this;
  }

  public toParamString(): string {
    if (this.paramMap.size === 0) {
      return '';
    }
    return Array.from(this.paramMap.keys())
      .map(key => [key, this.paramMap.get(key)])
      .reduce((previousValue, currentValue) => {
        const paramId = previousValue.length === 0 ? '?' : '&';
        return previousValue + `${paramId}${currentValue[0]}=${currentValue[1]}`
      }, '');
  }
}

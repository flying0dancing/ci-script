import { SentenceCasePipe } from "./sentence-case.pipe";

describe("SentenceCasePipe", () => {
  let pipe: SentenceCasePipe;

  beforeEach(() => {
    pipe = new SentenceCasePipe();
  });

  it("should return a tranformed value successfully", () => {
    expect(pipe.transform("hello")).toEqual("Hello");
    expect(pipe.transform("hello world")).toEqual("Hello world");
  });

  it("should return the value unchanged", () => {
    expect(pipe.transform(1 as any)).toEqual(1 as any);
    expect(pipe.transform(null)).toBeNull();
  });
});

export class FileLikeObject {
  rawFile: File;

  name: string;

  size: number;

  bytesAsSize: string;

  constructor(rawFile: File) {
    this.rawFile = rawFile;
    this.name = rawFile.name;
    this.size = rawFile.size;
    this.bytesAsSize = this.bytesToSize(rawFile.size);
  }

  private bytesToSize(bytes: number) {
    if (bytes === 0) {
      return "0 Bytes";
    }

    const k = 1024;
    const decimals = 2;
    const sizes = ["Bytes", "KB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB"];
    const i = Math.floor(Math.log(bytes) / Math.log(k));

    return (
      parseFloat((bytes / Math.pow(k, i)).toFixed(decimals)) + " " + sizes[i]
    );
  }
}

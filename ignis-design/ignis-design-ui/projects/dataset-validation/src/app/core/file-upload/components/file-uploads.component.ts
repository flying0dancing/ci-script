import { Component, Input, OnInit } from '@angular/core';

@Component({
  selector: 'dv-file-uploads',
  templateUrl: './file-uploads.component.html',
  styleUrls: ['./file-uploads.component.scss']
})
export class FileUploadsComponent implements OnInit {
  @Input() title?: string;

  constructor() {}

  ngOnInit() {}
}

import { NO_ERRORS_SCHEMA } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { MatDialog } from '@angular/material/dialog';
import { ShortcutsService } from '../../shortcuts/shortcuts.service';

import { HeaderComponent } from './header.component';

describe('HeaderComponent', () => {
  it('should create', () => {
    TestBed.configureTestingModule({
      declarations: [HeaderComponent],
      providers: [
        { provide: ShortcutsService, useValue: {} },
        { provide: MatDialog, useValue: {} }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    });

    const fixture = TestBed.createComponent(HeaderComponent);
    const component = fixture.componentInstance;

    expect(component).toBeDefined();
  });
});

import { ChangeDetectionStrategy, Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { take } from 'rxjs/operators';
import { FeedbackDialogComponent } from '../../dialogs/components/feedback-dialog.component';
import { ShortcutsService } from '../../shortcuts/shortcuts.service';

@Component({
  selector: 'dv-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class HeaderComponent implements OnInit {
  cheatSheetShortcut: string;

  constructor(
    private shortcutsService: ShortcutsService,
    public dialog: MatDialog
  ) {}

  ngOnInit(): void {
    this.cheatSheetShortcut = this.shortcutsService
      .findCheatSheetShortcut()
      .formatted[0].toUpperCase();
  }

  openCheatSheet() {
    this.shortcutsService.toggleCheatSheet();
  }

  openFeedbackDialog() {
    let feedbackDialog = this.dialog.open(FeedbackDialogComponent, {
      height: '400px',
      width: '600px'
    });

    feedbackDialog
      .afterClosed()
      .pipe(take(1))
      .subscribe(() => (feedbackDialog = undefined));
  }
}

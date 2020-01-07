import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ContainerComponent } from './container.component';

describe('ContainerComponent', () => {
  let component: ContainerComponent;
  let fixture: ComponentFixture<ContainerComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [ContainerComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(ContainerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  describe('size', () => {
    it('should default the `sizeClass` property  to `medium` for an invalid size', () => {
      component.size = 'UNKNOWN';
      fixture.detectChanges();
      expect(component.sizeClass).toEqual('medium');
    });

    it('should set the `sizeClass` property correctly for a valid size', () => {
      component.size = 'small';
      fixture.detectChanges();
      expect(component.sizeClass).toEqual('small');
    });
  });
});

import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ViewTaskCategoriesComponent } from './view-task-categories.component';

describe('ViewTaskCategoriesComponent', () => {
  let component: ViewTaskCategoriesComponent;
  let fixture: ComponentFixture<ViewTaskCategoriesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ViewTaskCategoriesComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(ViewTaskCategoriesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AddTaskCategoryComponent } from './add-task-category.component';

describe('AddTaskCategoryComponent', () => {
  let component: AddTaskCategoryComponent;
  let fixture: ComponentFixture<AddTaskCategoryComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AddTaskCategoryComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(AddTaskCategoryComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

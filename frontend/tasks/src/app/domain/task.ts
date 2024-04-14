import {TaskCategory} from "./task-category";

export class AddTaskViewForm {
  constructor(
    public name: string,
    public description: string,
    public deadline: Date,
    public category_name: string
  ) {}
}

export class AddTaskForm {
  constructor(
    public name: string,
    public description: string,
    public deadline: number, //epoch second
    public categoryId: number
  ) {}
}

export class EditTaskForm {
  constructor(
    public name: string | undefined,
    public description: string | undefined,
    public deadline: number | undefined, //epoch second
    public categoryId: number | undefined
  ) {}
}

export class Task {
  constructor(
    public id: number,
    public name: string,
    public description: string,
    public deadline: Date,
    public category: TaskCategory
  ) {}
}

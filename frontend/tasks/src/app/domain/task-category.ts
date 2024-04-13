export class TaskCategory {
  constructor(
    public id: number,
    public name: string,
    public description: string
  ) {}
}

export class AddTaskCategoryForm {
  constructor(
    public name: string,
    public description: string
  ) {}
}

export class EditTaskCategoryForm {
  constructor(
    public name: string | undefined,
    public description: string | undefined
  ) {}
}

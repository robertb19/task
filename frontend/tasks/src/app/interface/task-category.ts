export class TaskCategory {
  constructor(
    public id: number,
    public name: string,
    public description: string
  ) {}
}

export class Page {

  constructor(
    public pageNumber: number,
    public pageSize: number,
    public totalElements: number,
    public totalPages: number,
    public elements: any[]
  ) {}

}

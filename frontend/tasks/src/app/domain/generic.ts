export interface GenericAddResponse {
  id: number;
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

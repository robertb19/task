import {Injectable} from "@angular/core";
import {ActivatedRouteSnapshot, Resolve, RouterStateSnapshot} from "@angular/router";
import {Page} from "../interface/task-category";
import {map, Observable} from "rxjs";
import {TaskCategoriesService} from "../service/task-categories.service";

@Injectable()
export class TaskCategoryResolver implements Resolve<Page> {

  constructor(private taskCategoryService : TaskCategoriesService) {

  }

  resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot):  Observable<Page> {
    return this.taskCategoryService.get(0, 5, "desc")
  }

}

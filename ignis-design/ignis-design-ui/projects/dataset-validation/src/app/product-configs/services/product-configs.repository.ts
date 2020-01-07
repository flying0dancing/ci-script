import { HttpClient, HttpEvent, HttpRequest } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, Subscriber } from 'rxjs';
import { map } from 'rxjs/internal/operators';
import { EnvironmentService } from '../../../environments/environment.service';
import { Identifiable } from '../../core/utilities/interfaces/indentifiable.interface';
import { CreateProductConfigRequest } from '../interfaces/create-product-request.interface';
import { ProductConfig } from '../interfaces/product-config.interface';
import {
  ProductConfigTaskList,
  TaskStatus,
  TaskType,
  ValidationComplete,
  ValidationEvent
} from '../interfaces/product-validation.interface';
import { UpdateProductConfigRequest } from '../interfaces/update-product-request.interface';

@Injectable({ providedIn: 'root' })
export class ProductConfigsRepository {
  readonly productConfigsUrl: string;

  constructor(
    private http: HttpClient,
    private environmentService: EnvironmentService
  ) {
    this.productConfigsUrl = `${this.environmentService.env.api.root}/productConfigs`;
  }

  getOne(id: number): Observable<ProductConfig> {
    return this.http.get<ProductConfig>(`${this.productConfigsUrl}/${id}`);
  }

  getAll(): Observable<ProductConfig[]> {
    return this.http.get<ProductConfig[]>(this.productConfigsUrl);
  }

  create(request: CreateProductConfigRequest): Observable<number> {
    return this.http
      .post<Identifiable>(this.productConfigsUrl, request)
      .pipe(map(identifiable => identifiable.id));
  }

  updateVersion(
    id: number,
    request: UpdateProductConfigRequest
  ): Observable<ProductConfig> {
    return this.http.patch<ProductConfig>(
      `${this.productConfigsUrl}/${id}?type=product`,
      request
    );
  }

  delete(id): Observable<number> {
    return this.http
      .delete<Identifiable>(`${this.productConfigsUrl}/${id}`)
      .pipe(map(response => response.id));
  }

  upload(formData: FormData): Observable<HttpEvent<number>> {
    const request = new HttpRequest(
      'POST',
      `${this.productConfigsUrl}/file`,
      formData,
      {
        reportProgress: true
      }
    );

    return this.http.request<number>(request);
  }

  getTaskList(id: number): Observable<ProductConfigTaskList> {
    return this.http.get<ProductConfigTaskList>(
      `${this.productConfigsUrl}/${id}/tasks`
    );
  }

  validate(id: number): Observable<ValidationEvent> {
    return new Observable((sink: Subscriber<any>) => {
      const eventSource = new EventSource(
        `${this.productConfigsUrl}/${id}/validate`
      );

      eventSource.onmessage = (event: MessageEvent) => {
        const msg: ValidationEvent = JSON.parse(event.data);
        sink.next(msg);
      };

      //Http stream throws error on complete
      eventSource.onerror = () => {
        eventSource.close();
        const complete: ValidationComplete = {
          message: 'Event stream closed',
          status: TaskStatus.SUCCESS,
          type: TaskType.Complete
        };
        sink.next(complete);
      };

      //Close event source on unsubscribe
      sink.add(() => eventSource.close());
    });
  }
}

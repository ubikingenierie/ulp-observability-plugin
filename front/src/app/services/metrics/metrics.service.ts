import { Injectable } from '@angular/core';
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {Observable} from "rxjs";
import {environment} from "../../../environments/environment";

@Injectable({
  providedIn: 'root'
})
export class MetricsService {


  constructor(  private http: HttpClient) { }

  private headers = new HttpHeaders().set('Content-Type', 'text/plain; charset=utf-8');

  getMetrics(): Observable<String>{
         
         return this.http.get<String>('/ulp-o-metrics',{ headers: this.headers, responseType: 'text' as 'json'});
  }



}
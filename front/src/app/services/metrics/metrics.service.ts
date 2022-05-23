import { Injectable } from '@angular/core';
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {Observable} from "rxjs";
import { ServerInfo } from 'src/app/model/server-info';

@Injectable({
  providedIn: 'root'
})
export class MetricsService {

  private metricsURL : string = "/";
  constructor(  private http: HttpClient) { }

  private headers = new HttpHeaders().set('Content-Type', 'text/plain; charset=utf-8');



  setMetricsURL(url: string): void {
    this.metricsURL = url;
  }

  getAllMetrics(): Observable<String>{
    return this.http.get<String>(this.metricsURL+'?all',{ headers: this.headers, responseType: 'text' as 'json' });
  }

  getLastMetrics(): Observable<String>{
         return this.http.get<String>(this.metricsURL,{ headers: this.headers, responseType: 'text' as 'json' });
  }

  getMetricsServerInfo(): Observable<ServerInfo>{
    return this.http.get<ServerInfo>("/info",{ responseType: 'json' });
  }


}
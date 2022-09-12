import { Injectable } from '@angular/core';
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {Observable} from "rxjs";
import { PluginConfig } from 'src/app/model/plugin-config';

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

  getMetricsServerInfo(): Observable<PluginConfig>{
    return this.http.get<PluginConfig>("/config",{ responseType: 'json' });
  }


}
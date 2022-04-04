import UlpObservabilityMetric from "src/app/models/Metric";
import { collectHelp } from "./helpers/collectHelp";
import { collectType } from "./helpers/collectType";
import { collectValue } from "./helpers/collectValue";



const openMetricsToJson = (text: String) => {

    const metricsOnJson : Array<UlpObservabilityMetric> = [];
    let currentMetrics: UlpObservabilityMetric;
   
    let splitedMetrics = text.replace('\r\n', '\n').split('\n');
    splitedMetrics = splitedMetrics.filter(sm=> sm != "");
    let res : any;

    for(let i=0; i<splitedMetrics.length; i+=3){
      
      currentMetrics = {name: '', help:'', type: '', values :null};
      res = collectHelp(splitedMetrics[i]);
      currentMetrics = {name:res.name,
                        help:res.help,
                        type: collectType(splitedMetrics[i+1]),
                        values: collectValue(splitedMetrics[i+2])};
      metricsOnJson.push(currentMetrics);                  
    }      

    // Return JSON
    return metricsOnJson;
};


export {openMetricsToJson}
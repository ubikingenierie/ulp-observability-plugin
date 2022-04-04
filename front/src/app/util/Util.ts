import  Metric  from 'src/app/models/Metric';
import {ChartData} from '../models/ChartData';


//utils


const metricsToChartData = (metrics : Array<Metric>): ChartData => {

       const chartData: ChartData= {
           barChartLabels: [], barChartData: []
       };

       metrics.forEach(metric =>{

             chartData.barChartData.push({label: metric.name, data: [metric.values.value]});
             chartData.barChartLabels.push(new Date().toISOString());
            
            })

       return chartData;
}



const updateChartData = (chartData: ChartData, newMetrics : Array<Metric>) => {

  
        const updatedChartData: ChartData= {
            barChartLabels: [...chartData.barChartLabels], barChartData: [...chartData.barChartData]
        };

        newMetrics.forEach(metric =>{
            let i = updatedChartData.barChartData.findIndex(bc=>bc.label == metric.name);
            updatedChartData.barChartData[i] =  {...updatedChartData.barChartData[i], 
                data : [...updatedChartData.barChartData[i].data, metric.values.value]};
               
        })
        
        return updatedChartData;

}



export {metricsToChartData, updateChartData}
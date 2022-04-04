
interface ChartData {

    barChartLabels : Array<any>;
    barChartData :Array<ChartLine>;
}

interface ChartLine {
    data : Array<number>;
    label: String;
}

export {ChartData, ChartLine}
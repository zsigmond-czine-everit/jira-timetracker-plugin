/*
 * Copyright (C) 2011 Everit Kft. (http://www.everit.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
var worklogSummaryGadget = {};
worklogSummaryGadget.getTemplate = function(args) {
	var gadget = this;
	gadget.showLoading();
	var title= gadget.getMsg("jttp.gadget.wlsummary.addscrean.name");
	title=title+ " : "+ args.selectedFilter.name;
	gadget.savePref("gadgetTitle",title);
	gadgets.window.setTitle(title);
	 gadget.projectOrFilterName=args.selectedFilter.name;
        var width = (gadgets.window.getViewportDimensions().width || 600) - 20;
        var height = Math.round(width / 2);
        var innerHeight=height-50; 
        var sum=0;
        if(gadget.getPref("display")==="PieChart"){
        	for (i = 0; i < args.worlogSummaryData.length; i++) { 
        		sum+= args.worlogSummaryData[i].data;
        	}
        	   var html = '<div class="jttp-gadget" style="height: ' + height + 'px; width: ' + width + 'px">'
        	   + '<div class="aui-group aui-group-trio gadget-header">'
        	   + '<div class="aui-item">'+gadget.getMsg("jttp.gadget.settings.groupby")+' '+gadget.getPref("groupBy")+'</div>'
        	   + '<div class="aui-item">'+gadget.getMsg("jttp.gadget.settings.period")+' '+gadget.getPref("period")+'</div>'
        	   + '<div class="aui-item">'+gadget.getMsg("jttp.gadget.total_hours")+' '+ (+(Math.round(sum + "e+2")  + "e-2"))+ 'h</div>'
        	   +'</div>'
               + '<div id="jttp-gadget-piechart" class="chart" style="margin: 10px;  height: ' + innerHeight + 'px; width: ' + width + 'px"></div>'
               +'</div>';
              gadget.getView().html(html);
              gadget.showView(false,false);
        	  var plot = AJS.$.plot( AJS.$("#jttp-gadget-piechart"), args.worlogSummaryData, {
      			  series: {
      		           pie: { 
      			            show: true,
      			            radius: 1,
      			     }
      			  },
      			  grid: {
                      hoverable: true
                  },
              	   legend: {
                       show: true,
              	  },
              	 tooltip: true,
                 tooltipOpts: {
                     content: function(label,x,y){
                       return  "<strong>" + gadget.getMsg("jttp.gadget.time_spent") + "</strong><br><strong>"
                         + (+(Math.round(y + "e+2")  + "e-2"))+ "h"  +  "</strong>"
                     },
                     shifts: {
                         x: 20,
                         y: 0
                     },
                     defaultTheme: true
                 }
      		});
      } else {
    	  for (i = 0; i < args.worlogSummaryData.length; i++) { 
      		sum+=  args.worlogSummaryData[i].data[0][1];
      	}
        var barWidth=0.8;
        var barCharWidth=width-90;
        var html = '<div class="jttp-gadget-container" style="height: ' + height + 'px; width: ' + width + 'px">'
        + '<div class="aui-group aui-group-trio gadget-header">'
        + '<div class="aui-item">'+gadget.getMsg("jttp.gadget.settings.groupby")+' '+gadget.getPref("groupBy")+'</div>'
        + '<div class="aui-item">'+gadget.getMsg("jttp.gadget.settings.period")+' '+gadget.getPref("period")+'</div>'
        + '<div class="aui-item">'+gadget.getMsg("jttp.gadget.total_hours")+' '+gadget.getPref("groupBy")+' '+ (+(Math.round(sum + "e+2")  + "e-2"))+ "h</div>"
 	    + '</div>'
        + '<div id="jttp-gadget-chart" class="chart" style="float: left; height: ' + height + 'px; width: ' + barCharWidth + 'px"></div>'
        + '<div id="jttp-legendholder" style="padding-top: 5px;"></div>'
        +'</div>';
       var options = {
    	       series: { 
    	    		lines: {
    					fill: true,
    				},
    				 bars: {show: true,
                  	   barWidth: 0.8,
                  	   align: 'center',
                  	   fill: 1,
                  	   lineWidth: 0,
                  	   labelWidth: 10
                  	   }
    				},
    	                xaxis: {
    	    				mode: "categories",
    	    				tickLength: 0,
    	    				autoscaleMargin: 0.1
    	    			},
    	    			yaxis: {                
    	    			    tickFormatter: function (v, axis) {
    	    			        return v + " h";
    	    			    }
    	    			},        
    	    			grid: { hoverable: true, clickable: true },
    	    			legend: { 
    	    		    	  show: true,
    	    		    	  container: '#jttp-legendholder',
    	    		    	  labelFormatter: function(label, series) {
    	    		      	    // series is the series object for the label
    	    		      	    return label;
    	    		      	    }
    	    		         }
    	             };

        gadget.getView().html(html);
        gadget.showView(false,false);
        AJS.$.plot('#jttp-gadget-chart', args.worlogSummaryData, options);
        var previousPoint = null, previousLabel = null;
        AJS.$("#jttp-gadget-chart").bind("plothover", function (event, pos, item) {
            if (item) {
                if ((previousLabel != item.series.label) || (previousPoint != item.dataIndex)) {
                    previousPoint = item.dataIndex;
                    previousLabel = item.series.label;
                    AJS.$("#jttp-gadget-tooltip").remove();

                    var x = item.datapoint[0];
                    var y = item.datapoint[1];
                    var z = item.datapoint[2];
                    
                    var color = item.series.color;

                    //console.log(item.series.xaxis.ticks[x].label);                
                    var currentHeight=y-z;
                    worklogSummaryGadget.showTooltip(item.pageX,
                    item.pageY,
                    color,
                    "<strong>" + gadget.getMsg("jttp.gadget.time_spent") + "</strong><br><strong>"
                    + (+(Math.round(currentHeight + "e+2")  + "e-2"))+ "h"  +  "</strong>");
                }
            } else {
         	   AJS.$("#jttp-gadget-tooltip").remove();
                previousPoint = null;
            }
        });
        }
       gadget.hideLoading();
}
worklogSummaryGadget.showTooltip=function (x, y, color, contents) {
	AJS.$('<div id="jttp-gadget-tooltip">' + contents + '</div>').css({
        position: 'absolute',
        display: 'none',
        top: y - 40,
        left: x,
    })
	.addClass('gadget_tooltip')
    .appendTo("body").fadeIn(200);
}
worklogSummaryGadget.isEmpty= function (str){
	  return (!str || 0 === str.length);
}
worklogSummaryGadget.getDescriptor = function(args, gadget) {
	var gadget = gadget || this;
	var groupByOptions=[
	  {label:"Project", value: "Project"},
	  {label:"Issue", value: "Issue"},
	  {label:"User", value: "User"}];
	var display=[  {label:"Pie Chart", value: "PieChart"},
	               {label:"Bar Chart", value: "BarChart"}];
	var period=[{label:"Current Week", value: "CurrentWeek"},
	            {label:"Current Month", value: "CurrentMonth"},
	            {label:"Last 7 days", value: "Last7days"},
	            {label:"Last 30 days", value: "Last30days"} ];
	/*
	 * to access filterid use gadget.getPref("filterId")
	 * */
	var title= gadget.getPref("gadgetTitle");
	if (worklogSummaryGadget.isEmpty(title)){
		title=gadget.getMsg("jttp.gadget.wlsummary.addscrean.name");
	}
	gadget.savePref("gadgetTitle",title);
	gadgets.window.setTitle(title);
    return {
    	action: "/rest/jttp-rest/1/worklog-summary-gadget/validate",
        fields: [
           AJS.gadget.fields.filterPicker(gadget,"filterId"),
            {
                userpref: "groupBy",
                label: gadget.getMsg("jttp.gadget.settings.groupby"),
                type: "select",
                selected: gadget.getPref("groupBy"),
                options: groupByOptions
            }, {
                userpref: "display",
                label: gadget.getMsg("jttp.gadget.settings.display"),
                type: "select",
                selected: gadget.getPref("display"),
                options: display
            },  {
                userpref: "period",
                label: gadget.getMsg("jttp.gadget.settings.period"),
                type: "select",
                selected: gadget.getPref("period"),
                options: period
            },
            
            AJS.gadget.fields.nowConfigured()
            
        ] 
    };

};
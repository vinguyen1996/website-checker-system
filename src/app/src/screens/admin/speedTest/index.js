import React, { Component } from 'react';
import 'semantic-ui-css/semantic.min.css';
import { Segment, Button, Table, Icon, Dropdown } from 'semantic-ui-react'
import TableRow from './row-table';
import { Cookies } from "react-cookie";
import ReactToExcel from "react-html-table-to-excel";

const cookies = new Cookies();




class speedTestScreen extends Component {
    state = {
        list: [],
        loadingTable: false,
        averageInteractiveTime: 0,
        averagePageLoadTime: 0,
        averageSize: 0,
        isDisable: false,
        isDoneTest: false,
        listReportId: [],
        dateOption: [],
        dateValue: null
    };


    componentDidMount() {
        var comp = [];
        this.setState({ loadingTable: true });
        var param = {
            "userId": cookies.get("u_id"),
            "userToken": cookies.get("u_token"),
            "websiteId": cookies.get("u_w_id"),
            "pageOptionId": cookies.get("u_option"),
        }

        fetch("/api/speedTest/getHistoryList", {
            method: 'POST',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(param)
        }).then(response => response.json()).then((res) => {
            if (res.action === "SUCCESS") {
                if (res.data.length !== 0) {
                    var dateOption = res.data.map((item, index) => {
                        return { key: index, value: item, text: new Date(item).toLocaleString() };
                    })
                    console.log(dateOption);
                    this.setState({ dateOption: dateOption, dateValue: dateOption[0] })
                }
            }
        });

        fetch("/api/speedTest/lastest", {
            method: 'POST',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(param)
        }).then(response => response.json()).then((data) => {
            comp = data.speedtestReport.map((item, index) => {
                return (<TableRow key={index} url={item.url} interactiveTime={item.interactiveTime} pageLoadTime={item.pageLoadTime} size={item.size} />);
            });

            let totalInteractiveTime = data.speedtestReport.reduce((interactiveTime, item) => {
                return interactiveTime = +interactiveTime + +item.interactiveTime
            }, 0)

            let totalPageLoadTime = data.speedtestReport.reduce((pageLoadTime, item) => {
                return pageLoadTime = +pageLoadTime + +item.pageLoadTime
            }, 0)

            let totalSize = data.speedtestReport.reduce((sizePage, item) => {
                return sizePage = +sizePage + +item.size
            }, 0)

            let listLength = comp.length;
            let averageInteractiveTime = (totalInteractiveTime / listLength).toFixed(1);
            let averagePageLoadTime = (totalPageLoadTime / listLength).toFixed(1);
            let averageSize = (totalSize / listLength).toFixed(1);

            this.setState({
                averageInteractiveTime: averageInteractiveTime,
                averagePageLoadTime: averagePageLoadTime,
                averageSize: averageSize,
                list: comp,
                loadingTable: false
            });
        });


    }

    _clickUpdateListDate() {
        var param = {
            "userId": cookies.get("u_id"),
            "userToken": cookies.get("u_token"),
            "websiteId": cookies.get("u_w_id"),
            "pageOptionId": cookies.get("u_option"),
        }

        fetch("/api/speedTest/getHistoryList", {
            method: 'POST',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(param)
        }).then(response => response.json()).then((res) => {
            if (res.action === "SUCCESS") {
                if (res.data.length !== 0) {
                    var dateOption = res.data.map((item, index) => {
                        return { key: index, value: item, text: new Date(item).toLocaleString() };
                    })
                    console.log(dateOption);
                    this.setState({ dateOption: dateOption })
                }
            }
        });

    }

    _changeDate(event, data) {
        this.setState({ dateValue: data.value, loadingTable: true });
        var comp = [];
        fetch("/api/speedTest/getHistoryReport", {
            method: 'POST',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ reportTime: data.value })
        }).then(response => response.json()).then((res) => {
            if (res.action === "SUCCESS") {
                comp = res.data.map((item, index) => {
                    return (<TableRow key={index} url={item.url} interactiveTime={item.interactiveTime} pageLoadTime={item.pageLoadTime} size={item.size} />);
                });

                let totalInteractiveTime = res.data.reduce((interactiveTime, item) => {
                    return interactiveTime = +interactiveTime + +item.interactiveTime
                }, 0)

                let totalPageLoadTime = res.data.reduce((pageLoadTime, item) => {
                    return pageLoadTime = +pageLoadTime + +item.pageLoadTime
                }, 0)

                let totalSize = res.data.reduce((sizePage, item) => {
                    return sizePage = +sizePage + +item.size
                }, 0)

                let listLength = comp.length;
                let averageInteractiveTime = (totalInteractiveTime / listLength).toFixed(1);
                let averagePageLoadTime = (totalPageLoadTime / listLength).toFixed(1);
                let averageSize = (totalSize / listLength).toFixed(1);

                this.setState({
                    averageInteractiveTime: averageInteractiveTime,
                    averagePageLoadTime: averagePageLoadTime,
                    averageSize: averageSize,
                    list: comp,
                    loadingTable: false
                });
            }
        });
    }

    _doSpeedTest() {
        this.setState({ loadingTable: true });
        this.setState({ isDisable: true });
        var comp = [];
        var param = {
            "userId": cookies.get("u_id"),
            "userToken": cookies.get("u_token"),
            "websiteId": cookies.get("u_w_id"),
            "pageOptionId": cookies.get("u_option"),
        };

        fetch("/api/speedTest", {
            method: 'POST',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(param)
        }).then(response => response.json()).then((data) => {
            var listReport = [];
            comp = data.speedtestReport.map((item, index) => {
                listReport.push(item.id);
                return (<TableRow key={index} url={item.url} interactiveTime={item.interactiveTime} pageLoadTime={item.pageLoadTime} size={item.size} />);
            });

            let totalInteractiveTime = data.speedtestReport.reduce((interactiveTime, item) => {
                return interactiveTime = +interactiveTime + +item.interactiveTime
            }, 0)

            let totalPageLoadTime = data.speedtestReport.reduce((pageLoadTime, item) => {
                return pageLoadTime = +pageLoadTime + +item.pageLoadTime
            }, 0)

            let totalSize = data.speedtestReport.reduce((sizePage, item) => {
                return sizePage = +sizePage + +item.size
            }, 0)

            let listLength = comp.length;
            let averageInteractiveTime = (totalInteractiveTime / listLength).toFixed(1);
            let averagePageLoadTime = (totalPageLoadTime / listLength).toFixed(1);
            let averageSize = (totalSize / listLength).toFixed(1);

            this.setState({
                averageInteractiveTime: averageInteractiveTime,
                averagePageLoadTime: averagePageLoadTime,
                averageSize: averageSize,
                list: comp,
                loadingTable: false,
                isDisable: false,
                isDoneTest: true,
                listReportId: listReport
            });
        });
    }

    _saveReport() {
        this.setState({ loadingTable: true });
        this.setState({ isDisable: true });
        var comp = [];
        var param = {
            "userId": cookies.get("u_id"),
            "userToken": cookies.get("u_token"),
            "websiteId": cookies.get("u_w_id"),
            "pageOptionId": cookies.get("u_option"),
            "listReportId": this.state.listReportId
        };

        fetch("/api/speedTest/saveReport", {
            method: 'POST',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(param)
        }).then(response => response.json()).then((data) => {
            comp = data.speedtestReport.map((item, index) => {
                return (<TableRow key={index} url={item.url} interactiveTime={item.interactiveTime} pageLoadTime={item.pageLoadTime} size={item.size} />);
            });

            let totalInteractiveTime = data.speedtestReport.reduce((interactiveTime, item) => {
                return interactiveTime = +interactiveTime + +item.interactiveTime
            }, 0)

            let totalPageLoadTime = data.speedtestReport.reduce((pageLoadTime, item) => {
                return pageLoadTime = +pageLoadTime + +item.pageLoadTime
            }, 0)

            let totalSize = data.speedtestReport.reduce((sizePage, item) => {
                return sizePage = +sizePage + +item.size
            }, 0)

            let listLength = comp.length;
            let averageInteractiveTime = (totalInteractiveTime / listLength).toFixed(1);
            let averagePageLoadTime = (totalPageLoadTime / listLength).toFixed(1);
            let averageSize = (totalSize / listLength).toFixed(1);

            this.setState({
                averageInteractiveTime: averageInteractiveTime,
                averagePageLoadTime: averagePageLoadTime,
                averageSize: averageSize,
                list: comp,
                loadingTable: false,
                isDisable: false,
                isDoneTest: false
            });
        });
    }



    render() {
        return (
            <Segment.Group>
                <Segment style={{ border: 0, minWidth: 300 }}>
                    <Button icon primary labelPosition='right' disabled={this.state.isDisable} onClick={() => this._doSpeedTest()}>
                        Check
                       <Icon name='right arrow' />
                    </Button>
                    {this.state.isDoneTest ? <Button icon color="green" labelPosition='right' onClick={() => this._saveReport()}>
                        Save <Icon name='check' />
                    </Button> : ""}

                    <div style={{ float: 'right' }}>
                        <Dropdown style={{ marginRight: 10, zIndex: 9999 }} placeholder='Select history' selection defaultValue={this.state.dateValue} options={this.state.dateOption} value={this.state.dateValue} onClick={() => this._clickUpdateListDate()} onChange={(event, data) => this._changeDate(event, data)} />
                        <ReactToExcel
                            className="btn1"
                            table="table-to-xls"
                            filename="speed_test_file"
                            sheet="sheet 1"
                            buttonText={<Button color="green" ><Icon name="print" />Export</Button>}
                        />
                    </div>

                    {/* <Input icon='search' placeholder='Search...' style={{ float: 'right', marginRight: 5 }} /> */}
                </Segment>

                <Segment.Group horizontal >
                    <Segment basic loading={this.state.loadingTable} >

                        <Segment.Group horizontal >

                            <Segment style={{ paddingLeft: '10px' }}>
                                <p style={{ fontSize: 24 }}>{isNaN(this.state.averageInteractiveTime) ? 0 : this.state.averageInteractiveTime}s <br />
                                    Page interactive time</p>

                            </Segment >

                            <Segment>
                                <p style={{ fontSize: 24 }}>{isNaN(this.state.averagePageLoadTime) ? 0 : this.state.averagePageLoadTime}s <br /> Page load time</p>
                            </Segment>
                            <Segment>
                                <p style={{ fontSize: 24 }}>{isNaN(this.state.averageSize) ? 0 : this.state.averageSize} MB <br /> Average page size</p>
                            </Segment>
                        </Segment.Group>

                        <Segment basic style={{ maxHeight: '39vh', overflow: "auto" }}>

                            <Table unstackable id="table-to-xls">
                                <Table.Header >
                                    <Table.Row>
                                        <Table.HeaderCell>Page</Table.HeaderCell>
                                        <Table.HeaderCell>Interactive time</Table.HeaderCell>
                                        <Table.HeaderCell>Load time</Table.HeaderCell>
                                        <Table.HeaderCell>Size</Table.HeaderCell>

                                    </Table.Row>
                                </Table.Header>
                                <Table.Body>

                                    {this.state.list.length === 0 ? <Table.Row><Table.Cell>This page haven't test yet, please try to test</Table.Cell></Table.Row> : this.state.list}


                                </Table.Body>
                            </Table>

                        </Segment>
                    </Segment>

                </Segment.Group>
            </Segment.Group>

        );
    }



}

export default speedTestScreen;
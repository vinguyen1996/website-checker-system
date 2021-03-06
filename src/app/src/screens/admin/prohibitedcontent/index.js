import React, { Component } from 'react';
import { Segment, Button, Icon, Table, Dropdown } from 'semantic-ui-react'
import TableRow from './row-table';
import { Cookies } from "react-cookie";
import ReactToExcel from "react-html-table-to-excel";



const cookies = new Cookies();
export default class ProhibitedContent extends Component {


    state = { list: [], loadingTable: false, isDisable: false, tested: false, isDoneTest: false, listReportId: [], dateOption: [], dateValue: null };


    componentDidMount() {
        var comp = [];
        this.setState({ loadingTable: true });
        var param = {
            "userId": cookies.get("u_id"),
            "userToken": cookies.get("u_token"),
            "websiteId": cookies.get("u_w_id"),
            "pageOptionId": cookies.get("u_option"),
        }

        fetch("/api/prohibitedContent/getHistoryList", {
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


        fetch("/api/prohibitedContent/lastest", {
            method: 'POST',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(param)
        }).then(response => response.json()).then((data) => {
            comp = data.prohibitedContentReport.map((item, index) => {
                return (<TableRow key={index} urlPage={item.urlPage} word={item.word} fragment={item.fragment} type={item.type} />);
            });
            this.setState({ list: comp });
            this.setState({ loadingTable: false });
        });


    }

    _changeDate(event, data) {
        var comp = [];
        this.setState({ dateValue: data.value, loadingTable: true });
        fetch("/api/prohibitedContent/getHistoryReport", {
            method: 'POST',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ reportTime: data.value })
        }).then(response => response.json()).then((res) => {
            comp = res.data.map((item, index) => {
                return (<TableRow key={index} urlPage={item.urlPage} word={item.word} fragment={item.fragment} type={item.type} />);
            });
            this.setState({ list: comp });
            this.setState({ loadingTable: false });
        });

    }


    _clickUpdateListDate() {
        var param = {
            "userId": cookies.get("u_id"),
            "userToken": cookies.get("u_token"),
            "websiteId": cookies.get("u_w_id"),
            "pageOptionId": cookies.get("u_option"),
        }

        fetch("/api/prohibitedContent/getHistoryList", {
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

    _doProhibitedContent() {
        var comp = [];
        this.setState({ loadingTable: true, isDisable: true });
        var param = {
            "userId": cookies.get("u_id"),
            "userToken": cookies.get("u_token"),
            "websiteId": cookies.get("u_w_id"),
            "pageOptionId": cookies.get("u_option"),
        }

        fetch("/api/prohibitedContent", {
            method: 'POST',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(param)
        }).then(response => response.json()).then((data) => {
            var listReport = [];
            comp = data.prohibitedContentReport.map((item, index) => {
                listReport.push(item.id);
                return (<TableRow key={index} urlPage={item.urlPage} word={item.word} fragment={item.fragment} type={item.type} />);
            });
            this.setState({ list: comp });
            if (this.state.list.length === 0) {
                this.setState({ tested: true });
            }
            this.setState({ loadingTable: false, isDisable: false, isDoneTest: true, listReportId: listReport });
        });

    }

    _saveReport() {
        var comp = [];
        this.setState({ loadingTable: true, isDisable: true });
        var param = {
            "userId": cookies.get("u_id"),
            "userToken": cookies.get("u_token"),
            "websiteId": cookies.get("u_w_id"),
            "pageOptionId": cookies.get("u_option"),
            "listReportId": this.state.listReportId
        }

        fetch("/api/prohibitedContent/SaveReport", {
            method: 'POST',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(param)
        }).then(response => response.json()).then((data) => {
            comp = data.prohibitedContentReport.map((item, index) => {
                return (<TableRow key={index} urlPage={item.urlPage} word={item.word} fragment={item.fragment} type={item.type} />);
            });
            this.setState({ list: comp });
            this.setState({ loadingTable: false, isDisable: false, isDoneTest: false });
        });
    }





    render() {
        return (

            <Segment.Group horizontal style={{ margin: 0 }}>

                <Segment basic loading={this.state.loadingTable} >
                    <Button icon primary labelPosition='right' disabled={this.state.isDisable} onClick={() => this._doProhibitedContent()}>
                        Check
                       <Icon name='right arrow' />
                    </Button>
                    {this.state.isDoneTest && this.state.list.length !== 0 ? <Button icon color="green" labelPosition='right' onClick={() => this._saveReport()}>
                        Save <Icon name='check' />
                    </Button> : ""}
                    <div style={{ marginBottom: '10px', float: 'right' }}>
                        <Dropdown style={{ marginRight: 10, zIndex: 9999 }} placeholder='Select history' selection defaultValue={this.state.dateValue} options={this.state.dateOption} value={this.state.dateValue} onClick={() => this._clickUpdateListDate()} onChange={(event, data) => this._changeDate(event, data)} />


                        <ReactToExcel
                            className="btn1"
                            table="table-to-xls"
                            filename="prohibited_test_file"
                            sheet="sheet 1"
                            buttonText={<Button color="green"><Icon name="print" />Export</Button>}
                        />
                    </div>

                    <div style={{ marginBottom: '10px', float: 'right' }}>
                        {/* <Input icon='search' placeholder='Search...' /> */}
                    </div>
                    <Table singleLine unstackable style={{ fontSize: '16px', }} id="table-to-xls">
                        <Table.Header textAlign='center'>
                            <Table.Row>

                                <Table.HeaderCell>Word</Table.HeaderCell>
                                <Table.HeaderCell>Type</Table.HeaderCell>
                                <Table.HeaderCell>Fragment</Table.HeaderCell>
                                <Table.HeaderCell>Page</Table.HeaderCell>


                            </Table.Row>
                        </Table.Header>
                        <Table.Body>
                            {this.state.list.length === 0 ? <Table.Row><Table.Cell>{this.state.tested ? "This site has no prohibited content!" : "This page haven't test yet, please try to test!"}</Table.Cell></Table.Row> : this.state.list}


                        </Table.Body>
                    </Table>
                </Segment>


            </Segment.Group>

        );
    }
}
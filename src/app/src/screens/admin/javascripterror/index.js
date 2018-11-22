import React, { Component } from 'react';
import { Segment, Button, SegmentGroup, Table, Input, Icon } from 'semantic-ui-react'
import TableRow from './row-table';
import { Cookies } from "react-cookie";

const cookies = new Cookies();
export default class JavascriptErrorScreen extends Component {
  state = { list: [], loadingTable: false, isDisable: false };

  componentDidMount() {
    var comp = [];
    this.setState({ loadingTable: true });

    var param = {
      "userId": cookies.get("u_id"),
      "userToken": cookies.get("u_token"),
      "websiteId": cookies.get("u_w_id"),
      "pageOptionId": cookies.get("u_option"),
    };
    fetch("/api/jsTest/lastest", {
      method: 'POST',
      headers: {
        'Accept': 'application/json',
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(param)
    }).then(response => response.json()).then((data) => {
      console.log(data)
      // if (data.jsErrorReport.length !== 0) {
      comp = data.jsErrorReport.map((item, index) => {
        var msg = item.messages.replace("-", "");
        msg = msg.replace(msg.split(" ")[0], "");
        return (<TableRow key={index} page={item.pages} type={item.type} messages={msg} />);
      });
      this.setState({ list: comp });
      // }
      this.setState({ loadingTable: false });
    });

  }

  _doJSTest() {
    this.setState({ loadingTable: true });
    this.setState({ isDisable: true });
    var comp = [];
    var param = {
      "userId": cookies.get("u_id"),
      "userToken": cookies.get("u_token"),
      "websiteId": cookies.get("u_w_id"),
      "pageOptionId": cookies.get("u_option"),
    };

    fetch("/api/jsTest", {
      method: 'POST',
      headers: {
        'Accept': 'application/json',
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(param)
    }).then(response => response.json()).then((data) => {
      console.log(data.jsErrorReport)
      // if (data.jsErrorReport.length !== 0) {
      comp = data.jsErrorReport.map((item, index) => {

        var msg = item.messages.replace("-", "");
        msg = msg.replace(msg.split(" ")[0], "");
        if (data.type !== "WARNING") {
          var messages = msg.split(" at");
        }
        return (<TableRow key={index} page={item.pages} type={item.type} messages={messages} />);
      });
      this.setState({ list: comp });
      // }
      this.setState({ loadingTable: false });
    });
  }
  render() {
    return (
      <SegmentGroup vertical='true'>
        <Segment.Group horizontal>

          <Segment basic loading={this.state.loadingTable}>
            <Button icon labelPosition='right' disabled={this.state.isDisable} onClick={() => this._doJSTest()}>
              Check
                       <Icon name='right arrow' />
            </Button>
            <Button style={{ marginLeft: '10px' }} floated='right'><Icon name="print" />Export</Button>
            <div style={{ marginBottom: '10px', float: 'right' }}>
              <Input icon='search' placeholder='Search...' />
            </div>
            <Table unstackable singleLine style={{ fontSize: '14px' }}>
              <Table.Header>
                <Table.Row>
                  <Table.HeaderCell>Error Message</Table.HeaderCell>
                  <Table.HeaderCell>Type</Table.HeaderCell>
                  <Table.HeaderCell>Pages</Table.HeaderCell>
                </Table.Row>
              </Table.Header>
              <Table.Body>
                {this.state.list.length === 0 ? <Table.Row><Table.Cell>This page haven't test yet, please try to test</Table.Cell></Table.Row> : this.state.list}
              </Table.Body>
            </Table>
          </Segment>
        </Segment.Group>
      </SegmentGroup>
    );
  }
}
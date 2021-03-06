import PropTypes from 'prop-types'
import React, { Component } from 'react'
import {
    Button,
    Container,
    Grid,
    Header,
    Icon,
    Image,
    List,
    Menu,
    Responsive,
    Segment,
    Sidebar,
    Visibility,
    Input,
    TextArea
} from 'semantic-ui-react'
import 'semantic-ui-css/semantic.min.css';
import logo from '../../assets/icon-wsc.png';
import wallpaper from '../../assets/wallpaper.jpg';
import laptop from '../../assets/laptop.png';
import {
    Link
} from "react-router-dom";
//layout 
const HomepageHeading = ({ mobile }) => (
    <Container text>
        <Header
            as='h1'
            content="WCS'S TEAM IN LAB"
            inverted
            style={{
                fontSize: mobile ? '2em' : '4em',
                fontWeight: 'normal',
                marginBottom: 0,
                marginTop: mobile ? '1.5em' : '1.0em',
            }}
        />
        <Header
            as='h2'
            content='SPELLING CHECK'
            inverted
            style={{
                fontSize: mobile ? '1.1em' : '1.2em',
                fontWeight: 'normal',
                marginTop: mobile ? '0.5em' : '1.5em',
            }}
        />
    </Container>
)



HomepageHeading.propTypes = {
    mobile: PropTypes.bool,
}


class DesktopContainer extends Component {
    state = { txtSearch: "", searchLoading: false, spellingTxt: "", resSpellingtxt: [], wordAndSuggest: [] }

    hideFixedMenu = () => this.setState({ fixed: false })
    showFixedMenu = () => this.setState({ fixed: true })

    componentDidMount() {
        fetch("/api/lab/initTrie", {
            method: 'POST',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            }
        }).then(() => {
        });
    }

    _doSpellTest() {
        var param = { paragraph: this.state.spellingTxt };
        var comp = [];
        fetch("/api/lab/spelling", {
            method: 'POST',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(param)
        }).then(response => response.json()).then((res) => {
            if (res.action === "SUCCESS") {
                comp = res.data.map((item, index) => {
                    if (item.suggest === null) {
                        return (<span style={{ float: 'left', fontSize: 22 }}>{item.name} &nbsp;</span>);
                    } else {
                        return (<span style={{ background: 'red', color: 'white', float: 'left', fontSize: 22 }}>{item.name} &nbsp;</span>);
                    }
                });
                this.setState({ wordAndSuggest: res.data, resSpellingtxt: comp });
            }
        });
    }

    _onChangeSpelling(event, data) {
        this.setState({ spellingTxt: data.value });

    }

    render() {
        const { children } = this.props
        const { fixed } = this.state

        return (
            <Responsive minWidth={Responsive.onlyTablet.minWidth}>
                <Visibility
                    once={false}
                    onBottomPassed={this.showFixedMenu}
                    onBottomPassedReverse={this.hideFixedMenu}
                >
                    <Segment
                        textAlign='center'
                        style={{ height: '100%', minHeight: '100vh', padding: '1em 0em', background: `url(${wallpaper})`, backgroundSize: 'cover' }}
                        vertical
                    >
                        <Menu
                            fixed={fixed ? 'top' : null}
                            inverted={!fixed}
                            pointing={!fixed}
                            secondary={!fixed}
                            size='large'
                        >
                            <Container>

                                <Menu.Item as='a' position="left" >
                                    <Image src={logo} style={{ width: '50px', height: 'auto' }} />
                                    <font style={{ marginLeft: '10px', fontSize: '20px', color: 'white' }}>
                                        WEBSITE CHECKER SYSTEM
                    </font>
                                </Menu.Item>
                                <Menu.Item as='a'>
                                    HOME
                </Menu.Item>
                                <Menu.Item as='a' >TESTS WHAT?</Menu.Item>
                                <Menu.Item as='a' >PRICING</Menu.Item>
                                <Menu.Item as='a' active>WCS LAB</Menu.Item>
                                <Menu.Item >
                                    <Button inverted={!fixed} as={Link} to={'/login'}>
                                        {/* <Link to={'/login'} style={{color:'white'}}>Log in</Link> */}
                                        Log in
                  </Button>
                                    <Button inverted={!fixed} primary={fixed} style={{ marginLeft: '0.5em' }}>
                                        Sign up
                  </Button>
                                </Menu.Item>
                            </Container>
                        </Menu>
                        <HomepageHeading />
                        <Segment.Group horizontal style={{ background: 'transparent' }}>
                            <Segment style={{ border: 0 }}>
                                <TextArea autoHeight placeholder='Please insert ...' value={this.state.spellingTxt} onChange={(event, data) => this._onChangeSpelling(event, data)} style={{ fontSize: 30, minHeight: 400, minWidth: '80%' }} />
                            </Segment>

                            <Button style={{ height: 100, margin: 'auto' }} onClick={() => this._doSpellTest()}>Test</Button>
                            <Segment style={{ border: 0 }}>
                                {/* <TextArea autoHeight disabled value={this.state.resSpellingtxt} style={{ fontSize: 30, minHeight: 400, minWidth: '80%' }} /> */}
                                <div style={{ background: 'rgb(240, 240, 240)', width: '80%', height: '100%', margin: 'auto' }}>{this.state.resSpellingtxt}</div>
                            </Segment>
                        </Segment.Group>
                        <Image style={{ position: 'relative', width: '80vh', margin: 'auto', top: '5vh', }} src={laptop} />

                    </Segment>

                </Visibility>

                {children}
            </Responsive>
        )
    }



    onChangeText(event) {
        this.setState({
            txtSearch: event.target.value
        });
    }

    onClickSearch() {
        if (this.state.txtSearch === "") { alert('Please input exact your website'); }
        else {
            this.setState({
                searchLoading: true
            });
        }
    }
}

DesktopContainer.propTypes = {
    children: PropTypes.node,
}

class MobileContainer extends Component {
    state = {}

    handlePusherClick = () => {
        const { sidebarOpened } = this.state

        if (sidebarOpened) this.setState({ sidebarOpened: false })
    }

    handleToggle = () => this.setState({ sidebarOpened: !this.state.sidebarOpened })

    render() {
        const { children } = this.props
        const { sidebarOpened } = this.state

        return (
            <Responsive maxWidth={Responsive.onlyMobile.maxWidth}>
                <Sidebar.Pushable>
                    <Sidebar as={Menu} animation='uncover' inverted vertical visible={sidebarOpened}>
                        <Menu.Item as='a'>
                            <Image src={logo} style={{ width: '50px', height: 'auto', margin: 'auto' }} />
                        </Menu.Item>
                        <Menu.Item as='a'>Website Checker System</Menu.Item>
                        <Menu.Item as='a' active>
                            Home
            </Menu.Item>
                        <Menu.Item as='a'>Tests What</Menu.Item>
                        <Menu.Item as='a'>Pricing</Menu.Item>
                        <Menu.Item as={Link} to="/login">Log in</Menu.Item>
                        <Menu.Item as='a'>Sign Up</Menu.Item>
                    </Sidebar>

                    <Sidebar.Pusher
                        dimmed={sidebarOpened}
                        onClick={this.handlePusherClick}
                        style={{ minHeight: '100vh' }}
                    >
                        <Segment
                            inverted
                            textAlign='center'
                            style={{ height: 'auto', minHeight: '100vh', padding: '1em 0em', background: `url(${wallpaper})`, backgroundSize: 'cover' }}
                            vertical
                        >
                            <Container>
                                <Menu inverted pointing secondary size='large'>
                                    <Menu.Item onClick={this.handleToggle}>
                                        <Icon name='sidebar' />
                                    </Menu.Item>
                                    <Menu.Item position='right'>
                                        <Button as={Link} to="/login" inverted>
                                            Log in
                    </Button>
                                        <Button as='a' inverted style={{ marginLeft: '0.5em' }}>
                                            Sign Up
                    </Button>
                                    </Menu.Item>
                                </Menu>
                            </Container>
                            <HomepageHeading mobile />

                            <Input size='big' label='https://' icon={<Icon name='search' inverted circular link />} placeholder='mysite.com' style={{ marginTop: '30px' }} />
                            <Image style={{ position: 'relative', width: '80vh', margin: 'auto', top: '5vh', }} src={laptop} />

                        </Segment>

                        {children}
                    </Sidebar.Pusher>
                </Sidebar.Pushable>
            </Responsive>
        )
    }
}

MobileContainer.propTypes = {
    children: PropTypes.node,
}

const ResponsiveContainer = ({ children }) => (
    <div>
        <DesktopContainer>{children}</DesktopContainer>
        <MobileContainer>{children}</MobileContainer>
    </div>
)

// ResponsiveContainer.propTypes = {
//   children: PropTypes.node,
// }

const HomepageLayout = () => (
    <ResponsiveContainer>

        <Segment inverted vertical style={{ padding: '5em 0em' }}>
            <Container>
                <Grid divided inverted stackable>
                    <Grid.Row>
                        <Grid.Column width={3}>
                            <Header inverted as='h4' content='About' />
                            <List link inverted>
                                <List.Item as='a'>Sitemap</List.Item>
                                <List.Item as='a'>Contact Us</List.Item>
                            </List>
                        </Grid.Column>
                        <Grid.Column width={3}>
                            <Header inverted as='h4' content='Services' />
                            <List link inverted>
                                <List.Item as='a'>Pricing</List.Item>
                                <List.Item as='a'>DNA FAQ</List.Item>
                                <List.Item as='a'>How To Test</List.Item>
                            </List>
                        </Grid.Column>
                        <Grid.Column width={7}>
                            <Header as='h4' inverted>
                                Website Checker System
              </Header>
                            <p>
                                Check your site, make it better, grow your bussiness
              </p>
                        </Grid.Column>
                    </Grid.Row>
                </Grid>
            </Container>
        </Segment>
    </ResponsiveContainer>
)
export default HomepageLayout
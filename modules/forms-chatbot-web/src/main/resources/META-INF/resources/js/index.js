import React, { Component } from 'react';
import ReactDOM from 'react-dom';
import { ThemeProvider } from 'styled-components';
import ChatBot from 'react-simple-chatbot';
import PropTypes from 'prop-types';

const {getFormEntriesByUserURL, getFormDefinitionURL, portletNamespace, saveFormEntryURL} = window.chatBotConstants;

const customClient = {
	botAvatar: 'https://i.imgur.com/rTo045S.png',
	title: 'Ouvidoria - MPRJ'
}

const theme = {
background: '#F5F8FB',
fontFamily: 'Helvetica Neue',
headerBgColor: '#2E3A49',
headerFontColor: '#FFF',
headerFontSize: '16px',
botBubbleColor: '#007EB4',
botFontColor: '#FFF !important',
userBubbleColor: '#FFF !important',
userFontColor: '#4A4A4A',
};

class Review extends Component {
	constructor(props) {
		super(props);

		this.state = {
			steps: []
		};
	}

	componentWillMount() {
		const {steps, mapper} = this.props;

		const obj = Object.assign([], steps).filter(result => result);

		const result = mapper.map((result, index) => {
			return {
				question: result.message,
				answer: obj[index].value
			}
		});

		this.setState({ steps: result });
	}

	render() {
		const { steps } = this.state;

		if (steps.length === 0) return;

		return (
			<div style={{ width: '100%' }}>
				<h3>Summary</h3>
				<table>
					<tbody>
						{steps.map(({question, answer}, index) => (
							<tr key={index}>
								<td>{question}</td>
								<td>{answer}</td>
							</tr>
						))}
					</tbody>
				</table>
			</div>
		);
	}
}

Review.propTypes = {
	steps: PropTypes.array,
};

Review.defaultProps = {
	steps: undefined,
};

class App extends Component {
	constructor(props) {
		super(props);

		this.state = {
			steps: [],
			opened: true
		}
	}

	componentWillMount() {
		this.getFormEntriesByUserURL();
		this.getFormDefinition();
		// this.saveFormEntry();
	}

	componentDidMount() {
		this.handleEnd = this.handleEnd.bind(this);
		this.toggle = this.toggle.bind(this);
	}

	getFormEntriesByUserURL() {
		fetch(getFormEntriesByUserURL).then(response => {
			response.json().then(result => {
				console.log(result, 'getFormEntriesByUserURL');
			})
		}).catch(error => console.log(error));
	}

	getFormDefinition() {
		fetch(getFormDefinitionURL).then(response => {
			response.json().then(result => {
				console.log(result, 'getFormDefinitionURL');

				let mapper = [];

				mapper.push(
					{
						id: 'hello',
						message: 'Olá! Bem-vindo à ouvidoria do MPRJ.',
						trigger: 'help'
					},
					{
						id: 'help',
						message: 'O que você gostaria de fazer hoje?',
						trigger: 'options'
					},
					{
						id: 'list',
						component: (
							<div>{'denuncias'}</div>
						),
						trigger: 'help'
					},
					{
						id: 'options',
						options: [
							{ value: 'identify', label: 'criar denúncia', trigger: 'go' },
							{ value: 'list', label: 'listar denúncias', trigger: 'list' }
						],
					},
					{
						id: 'review',
						component: <Review />,
						asMessage: true,
						trigger: 'update'
					},
					{
						id: 'update',
						message: 'Posso confirmar sua denúncia?',
						trigger: 'update-question'
					},
					{
						id: 'update-question',
						options: [
							{ value: 'sim', label: 'sim', trigger: 'end-message' },
							{ value: 'não', label: 'não', trigger: 'help' },
						]
					},
					{
						id: 'end-message',
						message: 'Denúncia cadastrada com sucesso!',
						trigger: 'thank-you'
					},
					{
						id: 'thank-you',
						message: 'Obrigado!',
						end: true
					},
					{
						id: 'go',
						message: 'Ok, vamos la.',
						trigger: 'create'
					}
				)

				const items = result.map((item, index, arr) => {
					if (index === 0) {
						return {
							id: 'create',
							message: item.message,
							trigger: item.trigger
						}
					}

					if (index === arr.length - 1) {
						return {
							id: item.id,
							user: true,
							trigger: 'update'
						}
					}

					return item;
				});

				console.log('items', items);

				mapper.push(...items);

				this.setState({steps: mapper});
			})
		}).catch(error => console.log(error));
	}

	handleEnd({renderedSteps, steps, values}) {
		const answers = renderedSteps.map(({id, message}) => {
			return {
				id,
				value: message
			}
		});

		fetch(saveFormEntryURL, {
			method: 'POST',
			body: JSON.stringify({answers}),
			headers:{
				'Content-Type': 'application/json'
			}
		}).then(res => res.json())
		.then(response => console.log('Success:', JSON.stringify(response)))
		.catch(error => console.error('Error:', error));
	}

	toggle({opened}) {
		this.setState({opened})
	}

	render() {
		const {steps, opened} = this.state;
		const {botAvatar} = customClient;

		if (steps.length === 0) return <div/>;

		return (
			<ThemeProvider theme={theme}>
				<ChatBot
					userDelay={500}
					opened={opened}
					handleEnd={this.handleEnd}
					floating
					steps={steps}
					botAvatar={botAvatar}
					toggleFloating={this.toggle}
					headerTitle={customClient.title}
				/>
			</ThemeProvider>
		);
	}
}

export default function(elementId) {
	ReactDOM.render(<App />, document.getElementById(elementId));
}
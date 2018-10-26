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
	headerBgColor: '#105566',
	headerFontColor: '#FFF',
	headerFontSize: '16px',
	botBubbleColor: '#358d98',
	botFontColor: '#FFF !important',
	userBubbleColor: '#FFF !important',
	userFontColor: '#4A4A4A',
};

class List extends Component {
	constructor(props) {
		super(props);

		this.state = {
			list: []
		};
	}

	componentWillMount() {
		this.getFormEntriesByUserURL();
	}

	getFormEntriesByUserURL() {
		fetch(getFormEntriesByUserURL).then(response => {
			response.json().then(result => {
				this.setState({list: result});
			})
		}).catch(error => console.log(error));
	}

	render() {
		const {list} = this.state;

		return (
			<div style={{ width: '100%' }}>
				<h5>Protocolo das denúncias</h5>
				<ul style={{
					margin: 0,
					padding: 0
				}}>
					{list.map(({id, createDate}, index) => (
						<li style={{
							listStyle: 'none',
							borderBottom: '1px solid #e3e3e3',
							marginTop: '0.5rem',
							paddingBottom: '0.5rem',
						}} key={index}>
							<small>{createDate}</small>
							<div>{id}</div>
						</li>
					))}
				</ul>
			</div>
		);
	}
}

List.propTypes = {
	steps: PropTypes.array,
};

List.defaultProps = {
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
		this.getFormDefinition();
	}

	componentDidMount() {
		this.handleEnd = this.handleEnd.bind(this);
		this.toggle = this.toggle.bind(this);
	}

	getFormDefinition() {
		fetch(getFormDefinitionURL).then(response => {
			response.json().then(result => {
				console.log(result, 'getFormDefinitionURL');

				let mapper = [];

				const date = new Date();

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
							<List />
						),
						trigger: 'help'
					},
					{
						id: 'options',
						options: [
							{ value: 'identify', label: 'Criar denúncia', trigger: 'go' },
							{ value: 'list', label: 'Acompanhar denúncias', trigger: 'list' },
						],
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
						message: `Obrigado, sua denúncia foi enviada com sucesso!\nEsse é o número do seu protocolo: ${date.getTime()}`,
						trigger: 'available'
					},
					{
						id: 'available',
						message: 'Como você avalia este chat?',
						trigger: 'emojis'
					},
					{
						id: 'emojis',
						options: [
							{value: 'good', label: '😃', trigger: 'thank-you'},
							{value: 'normal', label: '🙂', trigger: 'thank-you'},
							{value: 'bad', label: '🙁', trigger: 'thank-you'}
						]
					},
					{
						id: 'thank-you',
						message: 'Obrigado pela sua avaliação.',
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
		const answers = renderedSteps.map(({id, value}) => {
			return {
				id,
				value
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
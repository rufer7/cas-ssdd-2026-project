import type { PageServerLoad } from './$types';

export const load: PageServerLoad = async ({ fetch }) => {
	try {
		const response = await fetch('http://localhost:8080/api/events');

		if (!response.ok) {
			throw new Error(`Backend responded with status ${response.status}`);
		}

		const events = await response.json();

		return {
			events
		};
	} catch (error) {
		console.error('Failed to load events from backend:', error);

		return {
			events: [],
			error: error instanceof Error ? error.message : 'Failed to load events'
		};
	}
};


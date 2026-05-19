<script lang="ts">
	import welcomeFallback from '$lib/images/svelte-welcome.png';
	import welcome from '$lib/images/svelte-welcome.webp';

	import Counter from './Counter.svelte';

	import type { PageData } from './$types';

	export let data: PageData;
</script>

<svelte:head>
	<title>Home</title>
	<meta name="description" content="Svelte demo app" />
</svelte:head>

<section>
	<h1>
		<span class="welcome">
			<picture>
				<source srcset={welcome} type="image/webp" />
				<img src={welcomeFallback} alt="Welcome" />
			</picture>
		</span>

		to your new<br />SvelteKit app
	</h1>

	<h2>
		try editing <strong>src/routes/+page.svelte</strong>
	</h2>

	<Counter />

	<div class="events-section">
		<h2>Events from Backend</h2>
		{#if data.error}
			<p class="error">Error: {data.error}</p>
		{:else if data.events.length === 0}
			<p>No events loaded</p>
		{:else}
			<ul>
				{#each data.events as event (event.id)}
					<li>{event.name || JSON.stringify(event)}</li>
				{/each}
			</ul>
		{/if}
	</div>
</section>

<style>
	section {
		display: flex;
		flex-direction: column;
		justify-content: center;
		align-items: center;
		flex: 0.6;
	}

	h1 {
		width: 100%;
	}

	.welcome {
		display: block;
		position: relative;
		width: 100%;
		height: 0;
		padding: 0 0 calc(100% * 495 / 2048) 0;
	}

	.welcome img {
		position: absolute;
		width: 100%;
		height: 100%;
		top: 0;
		display: block;
	}

	.events-section {
		margin-top: 2rem;
		padding: 1rem;
		border: 1px solid #ddd;
		border-radius: 8px;
		width: 100%;
		max-width: 500px;
	}

	.events-section ul {
		list-style: none;
		padding: 0;
	}

	.events-section li {
		padding: 0.5rem;
		background-color: #f5f5f5;
		margin: 0.5rem 0;
		border-radius: 4px;
	}

	.error {
		color: #d32f2f;
		font-weight: bold;
	}
</style>
